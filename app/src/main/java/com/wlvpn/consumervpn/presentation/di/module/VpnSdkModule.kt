package com.wlvpn.consumervpn.presentation.di.module

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import com.wlvpn.consumervpn.BuildConfig
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToVpnContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract
import com.wlvpn.consumervpn.presentation.controller.NotificationController
import com.wlvpn.consumervpn.presentation.controller.VpnRestartController
import com.wlvpn.consumervpn.presentation.notifications.NotificationFactory
import com.wlvpn.vpnsdk.sdk.fetures.VpnSdk
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import com.wlvpn.vpnsdk.sdk.value.NotificationProvider
import com.wlvpn.vpnsdk.sdk.value.PartnerConfiguration
import com.wlvpn.vpnsdk.sdk.value.SdkConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VpnSdkModule {

    @Provides
    fun providesConnectivityManager(
        application: Application
    ): ConnectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun providesNotificationManager(
        application: Application
    ): NotificationManager =
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideVpnSdk(
        application: Application,
        notificationFactory: NotificationFactory
    ): VpnSdk {
        val sdkConfiguration = SdkConfiguration(
            PartnerConfiguration(
                apikey = application.getString(R.string.api_key),
                accountName = application.getString(R.string.account_name),
                authSuffix = application.getString(R.string.auth_suffix),
                accountCreationKey = application.getString(R.string.account_creation_key)
            ),
            NotificationProvider(
                application.resources.getInteger(R.integer.vpn_notification_id),
                notificationFactory.vpnNotification
            ),
            NotificationProvider(
                application.resources.getInteger(R.integer.revoked_notification_id),
                notificationFactory.revokedNotification
            )
        ).let {
            it.copy(
                clientConfiguration = it.clientConfiguration.copy(
                    client = BuildConfig.CLIENT,
                    versionName = BuildConfig.VERSION_NAME,
                    isDebugBuild = BuildConfig.DEBUG
                ),
                vpnApi = it.vpnApi.copy(
                    host = application.getString(R.string.endpoint_main_api),
                    mirrors = emptyList(),
                    logInEndpoint = application.getString(R.string.login_api),
                    refreshTokenEndpoint = application.getString(R.string.token_refresh_api),
                    protocolsEndpoint = application.getString(R.string.protocol_list_api),
                    serversEndpoint = application.getString(R.string.server_list_api),
                ),
                wireGuardApi = it.wireGuardApi.copy(
                    host = application.getString(R.string.wireguard_endpoint_main_api),
                    mirrors = emptyList(),
                    bearerAuthEndpoint = application.getString(R.string.wireguard_bearer_auth_api),
                    credentialsAuthEndpoint =
                    application.getString(R.string.wireguard_credentials_auth_api)
                )
            )
        }

        return VpnSdk.setup(
            application,
            sdkConfiguration
        ).let {
            (it as VpnSdk.Companion.SetupResponse.Success).vpnSdk
        }
    }

    @Provides
    @Singleton
    fun provideVpnAccount(
        vpnSdk: VpnSdk
    ): VpnAccount = vpnSdk.vpnAccount

    @Provides
    @Singleton
    fun provideVpnConnection(
        vpnSdk: VpnSdk,
    ): VpnConnection = vpnSdk.vpnConnection

    @Provides
    @Singleton
    fun providesNotificationController(
        application: Application,
        listenVpnStateInteractor: ListenVpnStateContract.Interactor,
        notificationFactory: NotificationFactory
    ) = NotificationController(
        application,
        application.getSystemService(NotificationManager::class.java),
        listenVpnStateInteractor,
        notificationFactory
    )

    @Provides
    @Singleton
    fun providesVpnResetController(
        application: Application,
        vpnConnection: VpnConnection,
        connectToVpnInteractor: ConnectToVpnContract.Interactor
    ): VpnRestartController =
        VpnRestartController(
            application,
            vpnConnection,
            connectToVpnInteractor
        )

    @Provides
    fun providesNotificationFactory(application: Application): NotificationFactory =
        NotificationFactory(application)
}
