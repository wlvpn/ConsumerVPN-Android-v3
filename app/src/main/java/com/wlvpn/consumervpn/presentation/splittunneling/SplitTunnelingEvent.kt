package com.wlvpn.consumervpn.presentation.splittunneling

import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings

sealed class SplitTunnelingEvent {
    object LoadingInProgress : SplitTunnelingEvent()
    object UnableToLoadFailure : SplitTunnelingEvent()

    data class AppsLoaded(
        val isSearchActive: Boolean = false,
        val searchQuery: String = "",
        val availableFilters: List<SplitTunnelSettings.Filter> = emptyList(),
        val selectedFilter: SplitTunnelSettings.Filter? = null,
        val apps: List<SplitTunnelSettings.App> = emptyList(),
        val excludedPackages: List<String> = emptyList()
    ) : SplitTunnelingEvent()
}