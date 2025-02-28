package com.wlvpn.consumervpn.domain.value.settings

import com.wlvpn.consumervpn.domain.value.ConnectionTarget

data class ConnectionSettings(
    var selectedProtocol: Protocol = Protocol.WireGuard,
    var selectedTarget: ConnectionTarget = ConnectionTarget.Fastest,
    var startupConnectOption: StartupConnectOption =  StartupConnectOption.None,
    val isThreatProtectionEnabled: Boolean = false
)