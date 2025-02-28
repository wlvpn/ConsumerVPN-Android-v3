package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectOnBootContract.Status
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.gateway.NetworkGateway
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.ExpiredAccountFailure
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.InvalidatedAccountFailure
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.NoNetworkFailure
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.Success
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.UnableToConnectFailure
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.UserNotLoggedFailure
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.VpnNotPreparedFailure
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status.VpnServiceFailure
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption.FastestServer
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption.LastServer
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption.None
import com.wlvpn.consumervpn.util.catchOrEmpty
import com.wlvpn.consumervpn.util.repeatFirstUntil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen

private const val NO_NETWORK_ATTEMPTS = 3
private const val NO_NETWORK_DELAY = 5000L
private const val AFTER_NETWORK_FOUND_DELAY = 3000L
private const val NO_CONNECTION_ATTEMPTS = 3
private const val NO_CONNECTION_DELAY = 5000L

class ConnectOnBootInteractor(
    private val settingsRepository: ConnectionSettingsRepository,
    private val networkGateway: NetworkGateway,
    private val externalServersGateway: ExternalServersGateway,
    private val connectToSelectedServerInteractor: ConnectToSelectedServerContract.DomainInteractor
) : ConnectOnBootContract.Interactor {

    override fun execute(): Flow<Status> = flow {
        val settings = settingsRepository.getConnectionSettings()
            .catchOrEmpty {
                emit(Status.NoSettingsFoundFailure)
            }.firstOrNull() ?: run { return@flow }

        when (settings.startupConnectOption) {
            FastestServer -> {
                settingsRepository.saveConnectionSettings(
                    settings.copy(selectedTarget = ConnectionTarget.Fastest)
                ).catchOrEmpty {
                    emit(Status.SaveConnectionSettingsFailure)
                }.firstOrNull() ?: run { return@flow }

                emit(connect())
            }

            LastServer -> emit(connect())
            None -> {
                emit(Status.Success)
                return@flow
            }
        }
    }

    private suspend fun connect(): Status {
        val isNetworkAvailable = networkGateway.isNetworkAvailable()
            .repeatFirstUntil { value, iteration ->
                delay(NO_NETWORK_DELAY * iteration)
                !value && iteration < NO_NETWORK_ATTEMPTS
            }
            .catchOrEmpty { emit(false) }
            .firstOrNull() ?: false

        if (isNetworkAvailable.not()) {
            return Status.NoNetworkFailure
        }

        // Let android start the network
        delay(AFTER_NETWORK_FOUND_DELAY)

        // Update servers as fire and forget operation, ignore result
        externalServersGateway.updateServers()
            .catchOrEmpty { }
            .firstOrNull()

        val connectionResult = connectToSelectedServerInteractor.execute()
            .retryWhen { _, attempt ->
                delay(NO_CONNECTION_DELAY * attempt)
                attempt < NO_CONNECTION_ATTEMPTS
            }.repeatFirstUntil { value, iteration ->
                delay(NO_CONNECTION_DELAY * iteration)
                value !is Success && iteration < NO_CONNECTION_ATTEMPTS
            }.catchOrEmpty { }
            .firstOrNull() ?: run { return Status.UnableToConnectFailure }

        return when (connectionResult) {
            is Success ->
                Status.Success

            ExpiredAccountFailure,
            InvalidatedAccountFailure,
            NoNetworkFailure,
            is UnableToConnectFailure,
            UserNotLoggedFailure ->
                Status.AuthenticationFailure

            VpnNotPreparedFailure ->
                Status.VpnNotPreparedFailure

            is VpnServiceFailure ->
                Status.ServiceFailure(connectionResult.errorCode)
        }
    }
}