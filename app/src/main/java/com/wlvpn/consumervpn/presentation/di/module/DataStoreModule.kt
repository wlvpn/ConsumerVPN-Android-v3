package com.wlvpn.consumervpn.presentation.di.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.wlvpn.consumervpn.data.ConnectionSettingsProto
import com.wlvpn.consumervpn.data.IKEv2SettingsProto
import com.wlvpn.consumervpn.data.OpenVpnSettingsProto
import com.wlvpn.consumervpn.data.WireGuardSettingsProto
import com.wlvpn.consumervpn.data.repository.serializer.ConnectionSettingsProtoSerializer
import com.wlvpn.consumervpn.data.repository.serializer.IKEv2SettingsProtoSerializer
import com.wlvpn.consumervpn.data.repository.serializer.OpenVpnSettingsProtoSerializer
import com.wlvpn.consumervpn.data.repository.serializer.WireGuardSettingsProtoSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val APP_PREFERENCES = "app_preferences"
val Context.dataStore by preferencesDataStore(name = APP_PREFERENCES)

private const val CONNECTION_SETTINGS_DB_NAME = "connection-settings.pb"
private const val OPENVPN_SETTINGS_DB_NAME = "openvpn-settings.pb"
private const val IKEV2_SETTINGS_DB_NAME = "ikev2-settings.pb"
private const val WIREGUARD_SETTINGS_DB_NAME = "wireguard-settings.pb"

val Context.connectionSettingsDataStore: DataStore<ConnectionSettingsProto> by dataStore(
    fileName = CONNECTION_SETTINGS_DB_NAME,
    serializer = ConnectionSettingsProtoSerializer
)

val Context.openVpnSettingsDataStore: DataStore<OpenVpnSettingsProto> by dataStore(
    fileName = OPENVPN_SETTINGS_DB_NAME,
    serializer = OpenVpnSettingsProtoSerializer
)

val Context.ikev2SettingsDataStore: DataStore<IKEv2SettingsProto> by dataStore(
    fileName = IKEV2_SETTINGS_DB_NAME,
    serializer = IKEv2SettingsProtoSerializer
)

val Context.wireGuardSettingsDataStore: DataStore<WireGuardSettingsProto> by dataStore(
    fileName = WIREGUARD_SETTINGS_DB_NAME,
    serializer = WireGuardSettingsProtoSerializer
)

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

    @Singleton
    @Provides
    fun provideAppDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> =
        appContext.dataStore

    @Singleton
    @Provides
    fun providesConnectionSettingsDataStore(@ApplicationContext appContext: Context):
            DataStore<ConnectionSettingsProto> = appContext.connectionSettingsDataStore

    @Singleton
    @Provides
    fun providesOpenVpnSettingsDataStore(@ApplicationContext appContext: Context):
            DataStore<OpenVpnSettingsProto> = appContext.openVpnSettingsDataStore

    @Singleton
    @Provides
    fun providesIKEv2SettingsDataStore(@ApplicationContext appContext: Context):
            DataStore<IKEv2SettingsProto> = appContext.ikev2SettingsDataStore

    @Singleton
    @Provides
    fun providesWireGuardSettingsDataStore(@ApplicationContext appContext: Context):
            DataStore<WireGuardSettingsProto> = appContext.wireGuardSettingsDataStore

}
