package com.wlvpn.consumervpn.presentation.login.validation

sealed class LoginEvent {
    object ExecutingLogin : LoginEvent()

    object Success : LoginEvent()

    object EmptyUsername : LoginEvent()
    object EmptyPassword : LoginEvent()

    object NoNetwork : LoginEvent()
    object TooManyAttempts : LoginEvent()
    object InvalidCredentials : LoginEvent()
    data class UnableToLogin(
        val message: String,
        val errorCode: Int
    ) : LoginEvent()

    data class Error(val message: String) : LoginEvent()
}