package com.wlvpn.consumervpn.domain.value.settings

sealed class SplitTunnelSettings {

    data class DisallowedApps(
        val excludedAppPackages: List<String>
    ) : SplitTunnelSettings()

    data class App(
        val name: String,
        val packageName: String,
        val isSystemApp: Boolean = false
    )

    enum class Filter {
        System,
        User
    }
}