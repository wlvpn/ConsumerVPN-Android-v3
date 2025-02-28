package com.wlvpn.consumervpn.presentation.util

sealed class Routes(val route: String) {
    object Login : Routes("login_screen")
    object ForgotPassword : Routes("forgot_password")
    object Home : Routes("home_screen")
    object Locations : Routes("locations_screen")
    object Settings : Routes("settings_scree")
}
