package com.wlvpn.consumervpn.domain.value.settings

private const val DEFAULT_OPENVPN_PORT = 443

sealed class ProtocolSettings {
    data class OpenVpn(
        var internetProtocol: InternetProtocol = InternetProtocol.TCP,
        var scramble: Boolean = false,
        var port: OpenVpnPort = OpenVpnPort.Normal(value = DEFAULT_OPENVPN_PORT),
        var autoReconnect: Boolean = false,
        val allowLan: Boolean = false,
        val overrideMtu: Boolean = false,
    ) : ProtocolSettings()

    data class IKEv2(
        val allowLan: Boolean = false
    ) : ProtocolSettings()

    data class Wireguard(
        val allowLan: Boolean = false
    ) : ProtocolSettings()
}