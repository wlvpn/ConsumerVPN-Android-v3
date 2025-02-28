package com.wlvpn.consumervpn.domain.value.settings

sealed class Protocol {
    object OpenVpn : Protocol()
    object IKEv2 : Protocol()
    object WireGuard : Protocol()
}
