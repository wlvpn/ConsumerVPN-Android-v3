package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsContract.Status
import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsContract.Status.Success
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import timber.log.Timber

private const val DEFAULT_OPENVPN_PORT = 443
private const val DEFAULT_SCRAMBLE_OPENVPN_PORT = 3074

class RetrieveAllConnectionSettingsInteractor(
    private val connectionSettingsRepository: ConnectionSettingsRepository,
    private val protocolSettingsRepository: ProtocolSettingsRepository,
    private val externalVpnSettingsGateway: ExternalVpnSettingsGateway
) : RetrieveAllConnectionSettingsContract.Interactor {

    override fun execute(): Flow<Status> =
        connectionSettingsRepository.getConnectionSettings()
            .flatMapConcat  { connectionSettings ->
                // Use selected protocol to obtain the protocol settings
                protocolSettingsRepository
                    .getSettingsByProtocol(connectionSettings.selectedProtocol)
                    .flatMapConcat { protocolSettings ->
                        externalVpnSettingsGateway.fetchAvailableVpnPorts(protocolSettings)
                            .catch { emit(emptyList()) }
                            .map { portList ->
                                // If port is 0, set the first port from the list or default
                                if (protocolSettings is ProtocolSettings.OpenVpn) {
                                    val portNumber = protocolSettings.port.run {
                                        when (this) {
                                            is OpenVpnPort.Scramble -> value
                                            is OpenVpnPort.Normal -> value
                                        }
                                    }

                                    if (portNumber == 0) {
                                        protocolSettings.apply {
                                            // Replace the port number with a valid default one
                                            // from the available list of ports
                                            port = when (port) {
                                                is OpenVpnPort.Normal ->
                                                    OpenVpnPort.Normal(
                                                        portList.find { it == DEFAULT_OPENVPN_PORT }
                                                            ?: portList.firstOrNull()
                                                            ?: DEFAULT_OPENVPN_PORT
                                                    )

                                                is OpenVpnPort.Scramble ->
                                                    OpenVpnPort.Scramble(
                                                        value = portList.firstOrNull()
                                                            ?: DEFAULT_SCRAMBLE_OPENVPN_PORT
                                                    )
                                            }
                                        }
                                    }

                                    Success(
                                        connectionSettings, protocolSettings, portList) as Status
                                } else {
                                    Success(
                                        connectionSettings, protocolSettings, portList) as Status
                                }
                            }
                    }
            }.catch {
                Timber.e(it, "Unable to retrieve the settings")
                emit(Status.UnableToRetrieveSettingsFailure)
            }
}