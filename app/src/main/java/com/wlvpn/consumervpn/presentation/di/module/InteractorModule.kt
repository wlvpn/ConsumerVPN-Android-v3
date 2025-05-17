package com.wlvpn.consumervpn.presentation.di.module

import com.wlvpn.consumervpn.application.interactor.GetUserSessionContract
import com.wlvpn.consumervpn.application.interactor.GetUserSessionInteractor
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectOnBootContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectOnBootInteractor
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationInteractor
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToVpnContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToVpnInteractor
import com.wlvpn.consumervpn.application.interactor.connectivity.DisconnectFromVpnContract
import com.wlvpn.consumervpn.application.interactor.connectivity.DisconnectFromVpnInteractor
import com.wlvpn.consumervpn.application.interactor.connectivity.FetchGeoLocationContract
import com.wlvpn.consumervpn.application.interactor.connectivity.FetchGeoLocationInteractor
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateInteractor
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCityLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCityLocationsInteractor
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCountryLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCountryLocationsInteractor
import com.wlvpn.consumervpn.application.interactor.location.SearchCityLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.SearchCityLocationsInteractor
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsInteractor
import com.wlvpn.consumervpn.application.interactor.login.LoginContract
import com.wlvpn.consumervpn.application.interactor.login.LoginInteractor
import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserContract
import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserInteractor
import com.wlvpn.consumervpn.application.interactor.logout.LogoutContract
import com.wlvpn.consumervpn.application.interactor.logout.LogoutInteractor
import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionContract
import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionInteractor
import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsInteractor
import com.wlvpn.consumervpn.application.interactor.settings.SaveConnectionSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveConnectionSettingsInteractor
import com.wlvpn.consumervpn.application.interactor.settings.SaveProtocolSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveProtocolSettingsInteractor
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectInteractor
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.gateway.GeoLocationGateway
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.gateway.NetworkGateway
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerDomainInteractor
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object InteractorModule {

    @Provides
    fun providesLoginInteractor(
        loginGateway: LoginGateway,
        externalServersGateway: ExternalServersGateway
    ): LoginContract.Interactor = LoginInteractor(loginGateway, externalServersGateway)

    @Provides
    fun providesGetUserCredentialsInteractor(loginGateway: LoginGateway):
            GetUserSessionContract.Interactor =
        GetUserSessionInteractor(loginGateway)

    @Provides
    fun providesSaveProtocolSettingsInteractor(
        protocolSettingsRepository: ProtocolSettingsRepository
    ): SaveProtocolSettingsContract.Interactor = SaveProtocolSettingsInteractor(
        protocolSettingsRepository
    )

    @Provides
    fun providesSaveConnectionSettingsInteractor(
        connectionSettingsRepository: ConnectionSettingsRepository
    ): SaveConnectionSettingsContract.Interactor =
        SaveConnectionSettingsInteractor(connectionSettingsRepository)

    @Provides
    fun providesRetrieveAllSettingsInteractor(
        connectionSettingsRepository: ConnectionSettingsRepository,
        protocolSettingsRepository: ProtocolSettingsRepository,
        externalVpnSettingsGateway: ExternalVpnSettingsGateway
    ): RetrieveAllConnectionSettingsContract.Interactor = RetrieveAllConnectionSettingsInteractor(
        connectionSettingsRepository,
        protocolSettingsRepository,
        externalVpnSettingsGateway
    )

    @Provides
    fun providesCountryLocationsInteractor(
        externalServersGateway: ExternalServersGateway,
        connectionSettingsRepository: ConnectionSettingsRepository
    ): RetrieveCountryLocationsContract.Interactor = RetrieveCountryLocationsInteractor(
        externalServersGateway, connectionSettingsRepository
    )

    @Provides
    fun providesCityLocationsInteractor(
        externalServersGateway: ExternalServersGateway,
        connectionSettingsRepository: ConnectionSettingsRepository
    ): RetrieveCityLocationsContract.Interactor =
        RetrieveCityLocationsInteractor(externalServersGateway, connectionSettingsRepository)

    @Provides
    fun providesSearchLocationsInteractor(
        externalServersGateway: ExternalServersGateway,
        connectionSettingsRepository: ConnectionSettingsRepository
    ): SearchCountryLocationsContract.Interactor =
        SearchCountryLocationsInteractor(externalServersGateway, connectionSettingsRepository)

    @Provides
    fun providesSearchCityLocationsInteractor(
        externalServersGateway: ExternalServersGateway,
        connectionSettingsRepository: ConnectionSettingsRepository
    ): SearchCityLocationsContract.Interactor =
        SearchCityLocationsInteractor(externalServersGateway, connectionSettingsRepository)

    @Provides
    fun providesConnectToSelectedServerDomainInteractor(
        connectionSettingsRepository: ConnectionSettingsRepository,
        protocolSettingsRepository: ProtocolSettingsRepository,
        connectivityGateway: VpnConnectivityGateway,
        networkGateway: NetworkGateway,
        externalVpnSettingsGateway: ExternalVpnSettingsGateway,
        loginGateway: LoginGateway
    ): ConnectToSelectedServerContract.DomainInteractor =
        ConnectToSelectedServerDomainInteractor(
            connectionSettingsRepository,
            protocolSettingsRepository,
            connectivityGateway,
            networkGateway,
            externalVpnSettingsGateway,
            loginGateway
        )

    @Provides
    fun providesConnectToVpnInteractor(
        connectToSelectedServerDomainInteractor:
        ConnectToSelectedServerContract.DomainInteractor
    ): ConnectToVpnContract.Interactor =
        ConnectToVpnInteractor(connectToSelectedServerDomainInteractor)

    @Provides
    @Singleton
    fun providesDisconnectFromVpnInteractor(
        connectivityGateway: VpnConnectivityGateway
    ): DisconnectFromVpnContract.Interactor =
        DisconnectFromVpnInteractor(connectivityGateway)

    @Provides
    fun providesListenVpnStateInteractor(
        connectivityGateway: VpnConnectivityGateway
    ): ListenVpnStateContract.Interactor =
        ListenVpnStateInteractor(connectivityGateway)

    @Provides
    fun providesConnectToLocationContract(
        connectToSelectedServerDomainInteractor: ConnectToSelectedServerContract.DomainInteractor,
        connectionSettingsRepository: ConnectionSettingsRepository
    ): ConnectToLocationContract.Interactor =
        ConnectToLocationInteractor(
            connectToSelectedServerDomainInteractor,
            connectionSettingsRepository
        )

    @Provides
    fun providesConnectOnBootInteractor(
        settingsRepository: ConnectionSettingsRepository,
        networkGateway: NetworkGateway,
        externalServersGateway: ExternalServersGateway,
        connectToSelectedServerInteractor: ConnectToSelectedServerContract.DomainInteractor
    ): ConnectOnBootContract.Interactor =
        ConnectOnBootInteractor(
            settingsRepository,
            networkGateway,
            externalServersGateway,
            connectToSelectedServerInteractor
        )

    @Provides
    fun providesFetchGeolocationInteractor(
        getGeoLocationGateway: GeoLocationGateway
    ): FetchGeoLocationContract.Interactor = FetchGeoLocationInteractor(getGeoLocationGateway)

    @Provides
    fun providesMigrateLegacyUserInteractor(
        loginGateway: LoginGateway
    ): MigrateLegacyUserContract.Interactor =
        MigrateLegacyUserInteractor(loginGateway)

    @Provides
    fun providesPrepareThreatProtectionInteractor(
        externalVpnSettingsGateway: ExternalVpnSettingsGateway
    ): PrepareThreatProtectionContract.Interactor =
        PrepareThreatProtectionInteractor(externalVpnSettingsGateway)

    @Provides
    fun providesLogoutInteractor(
        connectivityGateway: VpnConnectivityGateway,
        loginGateway: LoginGateway,
        connectionSettingsRepository: ConnectionSettingsRepository,
        protocolSettingsRepository: ProtocolSettingsRepository
    ): LogoutContract.Interactor = LogoutInteractor(
        connectivityGateway,
        loginGateway,
        connectionSettingsRepository,
        protocolSettingsRepository
    )

    @Provides
    fun providesSaveServerLocationToConnectInteractor(
        connectionSettingsRepository: ConnectionSettingsRepository
    ): SaveServerLocationToConnectContract.Interactor =
        SaveServerLocationToConnectInteractor(connectionSettingsRepository)
}