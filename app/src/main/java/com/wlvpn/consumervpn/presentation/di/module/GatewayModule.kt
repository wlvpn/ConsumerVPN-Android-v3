package com.wlvpn.consumervpn.presentation.di.module

import android.app.Application
import android.net.ConnectivityManager
import com.wlvpn.consumervpn.data.gateway.NetworkCapabilitiesGateway
import com.wlvpn.consumervpn.data.gateway.SdkExternalVpnSettingsGateway
import com.wlvpn.consumervpn.data.gateway.VpnSdkConnectivityGateway
import com.wlvpn.consumervpn.data.gateway.VpnSdkGeoLocationGateway
import com.wlvpn.consumervpn.data.gateway.VpnSdkLoginGateway
import com.wlvpn.consumervpn.data.gateway.VpnSdkServersGateway
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.gateway.GeoLocationGateway
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.gateway.NetworkGateway
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.FlowPreview

@InstallIn(SingletonComponent::class)
@Module
object GatewayModule {

    @Provides
    fun providesLoginGateway(vpnAccount: VpnAccount): LoginGateway =
        VpnSdkLoginGateway(vpnAccount)

    @OptIn(FlowPreview::class)
    @Provides
    fun providesExternalVpnGateway(vpnConnection: VpnConnection):
            ExternalVpnSettingsGateway = SdkExternalVpnSettingsGateway(vpnConnection)

    @OptIn(FlowPreview::class)
    @Provides
    fun providesExternalServersGateway(vpnConnection: VpnConnection):
            ExternalServersGateway = VpnSdkServersGateway(vpnConnection)

    @Provides
    fun providesVpnConnectivityGateway(
        application: Application,
        vpnConnection: VpnConnection
    ): VpnConnectivityGateway =
        VpnSdkConnectivityGateway(application, vpnConnection)

    @Provides
    fun providesNetworkGateway(
        connectivityManager: ConnectivityManager
    ): NetworkGateway =
        NetworkCapabilitiesGateway(connectivityManager)

    @Provides
    fun providesGeotLocationGateway(
        vpnConnection: VpnConnection
    ): GeoLocationGateway = VpnSdkGeoLocationGateway(vpnConnection)
}
