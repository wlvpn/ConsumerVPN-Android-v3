package com.wlvpn.consumervpn.domain.value.settings

sealed class InternetProtocol {
    object UDP : InternetProtocol()
    object TCP : InternetProtocol()
}