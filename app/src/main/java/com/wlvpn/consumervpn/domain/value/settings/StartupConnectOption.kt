package com.wlvpn.consumervpn.domain.value.settings

sealed class StartupConnectOption {
    object LastServer : StartupConnectOption()
    object FastestServer : StartupConnectOption()
    object None : StartupConnectOption()
}