package com.wlvpn.consumervpn.presentation.login.validation

sealed class LoginValidationEvent {
    object Success : LoginValidationEvent()
}