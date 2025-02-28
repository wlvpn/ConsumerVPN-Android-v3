package com.wlvpn.consumervpn.domain.value.settings

sealed class OpenVpnPort {
    data class Normal(
        val value: Int
    ) : OpenVpnPort()

    data class Scramble(
        val value: Int
    ) : OpenVpnPort()
}