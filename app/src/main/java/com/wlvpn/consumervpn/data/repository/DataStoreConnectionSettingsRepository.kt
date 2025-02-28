package com.wlvpn.consumervpn.data.repository

import androidx.datastore.core.DataStore
import com.wlvpn.consumervpn.data.ConnectionSettingsProto
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.CityTargetProto
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.CountryTargetProto
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.FastestTargetProto
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.SelectedTargetCase.CITY
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.SelectedTargetCase.COUNTRY
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.SelectedTargetCase.FASTEST
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.SelectedTargetCase.SELECTEDTARGET_NOT_SET
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.SelectedTargetCase.SERVER
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.ServerTargetProto
import com.wlvpn.consumervpn.data.ConnectionSettingsProto.StartupProto
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DataStoreConnectionSettingsRepository(
    private val connectionSettingsDataStore: DataStore<ConnectionSettingsProto>
) : ConnectionSettingsRepository {

    override fun getConnectionSettings(): Flow<ConnectionSettings> =
        flow {
           emit(connectionSettingsDataStore.data.first())
        }.map {
                ConnectionSettings(
                    startupConnectOption = when (it.startupConnectOption) {
                        StartupProto.FASTEST -> StartupConnectOption.FastestServer
                        StartupProto.NONE -> StartupConnectOption.None
                        StartupProto.LAST_SERVER -> StartupConnectOption.LastServer
                        else -> StartupConnectOption.None
                    },
                    selectedProtocol = when (it.selectedProtocol) {
                        ConnectionSettingsProto.ProtocolProto.OPENVPN -> Protocol.OpenVpn
                        ConnectionSettingsProto.ProtocolProto.IKEV2 -> Protocol.IKEv2
                        ConnectionSettingsProto.ProtocolProto.WIREGAURD -> Protocol.WireGuard
                        else -> Protocol.WireGuard
                    },
                    selectedTarget = when (it.selectedTargetCase ?: FASTEST) {
                        COUNTRY -> it.country.toTargetCountry()
                        CITY -> it.city.toTargetCity()
                        SERVER -> it.server.toTargetServer()
                        FASTEST -> it.fastest.toTargetFastest()
                        SELECTEDTARGET_NOT_SET -> it.fastest.toTargetFastest()
                    },
                    isThreatProtectionEnabled = it.threatProtection
                )
            }

    override fun saveConnectionSettings(connectionSettings: ConnectionSettings): Flow<Unit> = flow {
        connectionSettingsDataStore.updateData {
            it.toBuilder()
                .setStartupConnectOption(
                    when (connectionSettings.startupConnectOption) {
                        StartupConnectOption.None -> StartupProto.NONE
                        StartupConnectOption.LastServer -> StartupProto.LAST_SERVER
                        StartupConnectOption.FastestServer -> StartupProto.FASTEST
                })
                .setSelectedProtocol(
                    when (connectionSettings.selectedProtocol) {
                        Protocol.IKEv2 -> ConnectionSettingsProto.ProtocolProto.IKEV2
                        Protocol.OpenVpn -> ConnectionSettingsProto.ProtocolProto.OPENVPN
                        Protocol.WireGuard -> ConnectionSettingsProto.ProtocolProto.WIREGAURD
                    }
                ).apply {
                    when (val selectedTarget = connectionSettings.selectedTarget) {
                        is ConnectionTarget.City ->
                            city = selectedTarget.toProtoCity(city)
                        is ConnectionTarget.Country ->
                            country = selectedTarget.toProtoCountry(country)
                        is ConnectionTarget.Fastest ->
                            fastest = selectedTarget.toProtoFastest(fastest)
                        is ConnectionTarget.Server ->
                            server = selectedTarget.toProtoServer(server)
                    }
                }
                .setThreatProtection(connectionSettings.isThreatProtectionEnabled)
                .build()
        }
        emit(Unit)
    }

    override fun clearConnectionSettings(): Flow<Unit> = flow {
        connectionSettingsDataStore.updateData {
            it.toBuilder()
                .clear()
                .build()

        }
        emit(Unit)
    }

    // ConnectionSettingsProto.XTarget to ConnectionTarget
    private fun CountryTargetProto.toTargetCountry():
            ConnectionTarget.Country = ConnectionTarget.Country(code)

    private fun CityTargetProto.toTargetCity():
            ConnectionTarget.City = ConnectionTarget.City(country.toTargetCountry(), name)

    private fun ServerTargetProto.toTargetServer():
            ConnectionTarget.Server = ConnectionTarget.Server(city.toTargetCity(), name)

    private fun FastestTargetProto.toTargetFastest():
            ConnectionTarget.Fastest = ConnectionTarget.Fastest

    // ConnectionTarget to ConnectionSettingsProto.XTarget
    private fun ConnectionTarget.Country.toProtoCountry(
        proto: CountryTargetProto
    ): CountryTargetProto =
        proto.toBuilder()
            .setCode(code)
            .build()

    private fun ConnectionTarget.City.toProtoCity(
        proto: CityTargetProto
    ): CityTargetProto =
        proto.toBuilder()
            .setCountry(country.toProtoCountry(proto.country))
            .setName(name)
            .build()

    private fun ConnectionTarget.Server.toProtoServer(
        proto: ServerTargetProto
    ): ServerTargetProto =
        proto.toBuilder()
            .setCity(city.toProtoCity(proto.city))
            .setName(name)
            .build()

    private fun ConnectionTarget.Fastest.toProtoFastest(
        proto: FastestTargetProto
    ): FastestTargetProto = proto

}