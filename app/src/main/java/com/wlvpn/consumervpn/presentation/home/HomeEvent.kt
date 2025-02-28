package com.wlvpn.consumervpn.presentation.home

import com.wlvpn.consumervpn.domain.value.ServerLocation

sealed class HomeEvent {

    data class Connected(val server: ServerLocation.Server?) : HomeEvent()
    object Connecting : HomeEvent()
    object Disconnected : HomeEvent()
    object DisconnectedError : HomeEvent()
}