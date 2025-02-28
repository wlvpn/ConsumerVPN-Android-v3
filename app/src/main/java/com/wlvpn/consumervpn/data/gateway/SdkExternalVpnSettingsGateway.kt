package com.wlvpn.consumervpn.data.gateway

import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway.UnableToPrepareThreatProtectionFailure
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import com.wlvpn.consumervpn.util.asFlow
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.PrepareThreatProtectionResponse.ExpiredAccessToken
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.PrepareThreatProtectionResponse.ExpiredRefreshToken
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.PrepareThreatProtectionResponse.InvalidApiKey
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.PrepareThreatProtectionResponse.Success
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.PrepareThreatProtectionResponse.UnableToPrepareThreatProtection
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@FlowPreview
class SdkExternalVpnSettingsGateway(
    private val vpnConnection: VpnConnection
) : ExternalVpnSettingsGateway {

    override fun fetchAvailableVpnPorts(protocolSettings: ProtocolSettings): Flow<List<Int>> =
        when (protocolSettings) {
            is ProtocolSettings.OpenVpn -> vpnConnection.getOpenVpnAvailablePorts()
                .filter { it is VpnConnection.GetOpenVpnAvailablePortsResponse.Success }
                .map { it as VpnConnection.GetOpenVpnAvailablePortsResponse.Success }
                .map {
                    if (protocolSettings.scramble) {
                        it.openVpnPorts.scramblePorts
                    } else {
                        it.openVpnPorts.ports
                    }
                }

            is ProtocolSettings.IKEv2,
            is ProtocolSettings.Wireguard -> flowOf(emptyList())
        }

    override fun prepareThreatProtection(): Flow<Unit> =
        vpnConnection.prepareThreatProtection()
            .map {
                when (it) {
                    Success -> Unit.asFlow()

                    is UnableToPrepareThreatProtection ->
                        throw UnableToPrepareThreatProtectionFailure(it.throwable)

                    ExpiredAccessToken ->
                        throw ExternalVpnSettingsGateway.ExpiredAccessTokenFailure()

                    ExpiredRefreshToken ->
                        throw ExternalVpnSettingsGateway.ExpiredRefreshTokenFailure()

                    InvalidApiKey ->
                        throw ExternalVpnSettingsGateway.InvalidApiKeyFailure()
                }
            }
}