package com.wlvpn.consumervpn.domain.value

sealed class VpnConnectivityStatus {

    object Connected : VpnConnectivityStatus()

    object Connecting : VpnConnectivityStatus()

    object Disconnected : VpnConnectivityStatus()

    object Error : VpnConnectivityStatus()
}