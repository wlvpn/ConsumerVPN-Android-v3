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
        // A set so the screens can test a row for exclusion in constant time while the
        // list is being scrolled
        val excludedPackages: Set<String> = emptySet()
    ) : SplitTunnelingEvent() {

        /**
         * Apps matching the active search query and filter, this is what the screens render
         * and what the select/deselect all actions operate on to have a single source of truth.
         *
         * Resolved once per state instance instead of on every read, filtering the whole
         * installed app list on each recomposition is too expensive for a TV.
         */
        val visibleApps: List<SplitTunnelSettings.App> = filterVisibleApps(this)
    }
}