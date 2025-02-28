package com.wlvpn.consumervpn.domain.value

import com.wlvpn.consumervpn.domain.value.settings.Protocol

data class CurrentConnection(
    val timeConnected: Long,
    val server: ServerLocation.Server,
    val protocol: Protocol
)