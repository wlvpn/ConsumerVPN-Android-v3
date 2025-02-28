package com.wlvpn.consumervpn.domain.interactor

import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.gateway.NetworkGateway
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract.Status
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Connected
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Connecting
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Disconnected
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Error
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort.Normal
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort.Scramble
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEmpty

private const val DEFAULT_OPENVPN_PORT = 443
private const val DEFAULT_SCRAMBLE_OPENVPN_PORT = 3074

class ConnectToSelectedServerDomainInteractor(
    private val connectionSettingsRepository: ConnectionSettingsRepository,
    private val protocolSettingsRepository: ProtocolSettingsRepository,
    private val connectivityGateway: VpnConnectivityGateway,
    private val networkGateway: NetworkGateway,
    private val externalVpnSettingsGateway: ExternalVpnSettingsGateway,
    private val loginGateway: LoginGateway
) : ConnectToSelectedServerContract.DomainInteractor {

    override fun execute(): Flow<Status> = flow {
        val isLoggedIn = loginGateway.isLoggedIn().first()

        if (!isLoggedIn) {
            emit(Status.UserNotLoggedFailure)
            return@flow
        }

        val isVpnPrepared = connectivityGateway.isVpnPrepared().first()

        if (!isVpnPrepared) {
            emit(Status.VpnNotPreparedFailure)
            return@flow
        }

        val hasNetwork = networkGateway.isNetworkAvailable().first()

        if (!hasNetwork) {
            emit(Status.NoNetworkFailure)
            return@flow
        }

        when (connectivityGateway.currentVpnState().first()) {
            Connected,
            Connecting -> {
                connectivityGateway.disconnect().first()
                emit(connectToVpn())
            }

            Disconnected,
            Error -> emit(connectToVpn())
        }
    }

    private suspend fun connectToVpn(): Status {
        val connectionSetting = connectionSettingsRepository.getConnectionSettings().first()
        val protocolSettings =
            protocolSettingsRepository.getSettingsByProtocol(connectionSetting.selectedProtocol)
                .first()
                .run {
                    if (this is ProtocolSettings.OpenVpn) {
                        validateOpenVpnPort(this)
                    } else {
                        this
                    }
                }
        connectivityGateway.connect(
            target = connectionSetting.selectedTarget,
            protocolSettings = protocolSettings,
            connectionSettings = connectionSetting

        ).first()

        return Status.Success
    }

    private suspend fun validateOpenVpnPort(
        openVpnSettings: ProtocolSettings.OpenVpn
    ): ProtocolSettings {
        val portNumber = openVpnSettings.port.run {
            when (this) {
                is Normal -> value
                is Scramble -> value
            }
        }

        return when (portNumber) {
            0 -> {
                val ports = externalVpnSettingsGateway.fetchAvailableVpnPorts(openVpnSettings)
                    .catch { emit(emptyList()) }
                    .onEmpty { emit(emptyList()) }
                    .first()

                openVpnSettings.apply {
                    port = when (port) {
                        is Normal -> OpenVpnPort.Normal(
                            ports.find { it == DEFAULT_OPENVPN_PORT }
                                ?: ports.firstOrNull()
                                ?: DEFAULT_OPENVPN_PORT
                        )

                        is Scramble -> OpenVpnPort.Scramble(
                            ports.firstOrNull() ?: DEFAULT_SCRAMBLE_OPENVPN_PORT
                        )
                    }
                }
            }

            else -> openVpnSettings
        }
    }
}