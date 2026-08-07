package com.wlvpn.consumervpn.domain.gateway

import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings

interface SplitTunnelGateway {
    suspend fun getInstalledApps(): List<SplitTunnelSettings.App>
}