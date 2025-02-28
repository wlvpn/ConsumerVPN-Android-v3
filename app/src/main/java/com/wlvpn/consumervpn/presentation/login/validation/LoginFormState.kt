package com.wlvpn.consumervpn.presentation.login.validation

import com.wlvpn.consumervpn.presentation.util.UiText

data class LoginFormState(
    val username: String = "",
    val password: String = "",
    val usernameError: UiText? = null,
    val passwordError: UiText? = null,
    val sdkValidationError: UiText? = null,
    val showLoading: Boolean = false,
)