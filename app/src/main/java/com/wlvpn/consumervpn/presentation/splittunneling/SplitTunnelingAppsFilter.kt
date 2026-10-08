package com.wlvpn.consumervpn.presentation.splittunneling

import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings

/**
 * Filters the installed app list down to the entries matching the active search query and filter.
 */
fun filterVisibleApps(
    state: SplitTunnelingEvent.AppsLoaded
): List<SplitTunnelSettings.App> = state.apps.filter { app ->
    val matchesSearch = app.name.contains(state.searchQuery, ignoreCase = true)

    val matchesFilter = when (state.selectedFilter) {
        null -> true
        SplitTunnelSettings.Filter.System -> app.isSystemApp
        SplitTunnelSettings.Filter.User -> !app.isSystemApp
    }

    matchesSearch && matchesFilter
}
