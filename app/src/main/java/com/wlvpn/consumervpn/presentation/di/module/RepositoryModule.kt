package com.wlvpn.consumervpn.presentation.di.module

import androidx.datastore.core.DataStore
import com.wlvpn.consumervpn.data.ConnectionSettingsProto
import com.wlvpn.consumervpn.data.IKEv2SettingsProto
import com.wlvpn.consumervpn.data.OpenVpnSettingsProto
import com.wlvpn.consumervpn.data.WireGuardSettingsProto
import com.wlvpn.consumervpn.data.repository.DataStoreConnectionSettingsRepository
import com.wlvpn.consumervpn.data.repository.DataStoreProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun providesConnectionSettingsRepository(settingsStore: DataStore<ConnectionSettingsProto>):
            ConnectionSettingsRepository = DataStoreConnectionSettingsRepository(settingsStore)

    @Provides
    fun providesProtocolSettingsRepository(
        openVpnSettingsStore: DataStore<OpenVpnSettingsProto>,
        ikev2SettingsStore: DataStore<IKEv2SettingsProto>,
        wireGuardSettingsStore: DataStore<WireGuardSettingsProto>,
    ): ProtocolSettingsRepository =
        DataStoreProtocolSettingsRepository(
            openVpnSettingsStore,
            wireGuardSettingsStore,
            ikev2SettingsStore
        )
}