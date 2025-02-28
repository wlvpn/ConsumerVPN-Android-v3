package com.wlvpn.consumervpn.presentation.home

sealed class FailureEvent {

    data object NoNetworkError : FailureEvent()

}