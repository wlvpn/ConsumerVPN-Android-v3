package com.wlvpn.consumervpn.application.interactor.logout

import com.wlvpn.consumervpn.application.interactor.logout.LogoutContract.Status
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.util.catchOrEmpty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration.Companion.seconds

private const val LOGOUT_TIME_OUT_SECONDS = 15

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LogoutInteractor(
    private val connectivityGateway: VpnConnectivityGateway,
    private val loginGateway: LoginGateway,
    private val connectionSettingsRepository: ConnectionSettingsRepository,
    private val protocolSettingsRepository: ProtocolSettingsRepository
) : LogoutContract.Interactor {

    override fun execute(): Flow<Status> =
        connectivityGateway.disconnect()
            .catchOrEmpty {
                emit(Unit) // Ignore errors
            }.flatMapConcat {
                connectionSettingsRepository.clearConnectionSettings()
                    .catchOrEmpty {
                        emit(Unit) // Ignore errors
                    }
            }.flatMapConcat {
                protocolSettingsRepository.clearProtocolSettings()
                    .catchOrEmpty {
                        emit(Unit) // Ignore errors
                    }
            }.flatMapConcat {
                loginGateway.logout()
                    .map {
                        Status.Success as Status
                    }
            }.timeout(LOGOUT_TIME_OUT_SECONDS.seconds)
            .catch { throwable ->
                val error = when (throwable) {
                    is LoginGateway.ExpiredAccessTokenFailure -> Status.ExpiredAccessToken
                    is LoginGateway.InvalidAccessTokenFailure -> Status.InvalidAccessToken
                    is LoginGateway.UnableToLogoutFailure -> Status.UnableToLogout
                    else -> {
                        Status.UnableToLogout
                    }
                }
                emit(error)
            }
}