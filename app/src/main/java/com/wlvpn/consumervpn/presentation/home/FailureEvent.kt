package com.wlvpn.consumervpn.presentation.home

sealed class FailureEvent {

    data object NoNetworkError : FailureEvent()
    data object ExpiredWireGuardAccount : FailureEvent()
    data object InactiveWireGuardAccount : FailureEvent()
    data object InvalidWireGuardApiResponse : FailureEvent()
    data object UnableToConnect : FailureEvent()
    data object UserNotLogged : FailureEvent()
}