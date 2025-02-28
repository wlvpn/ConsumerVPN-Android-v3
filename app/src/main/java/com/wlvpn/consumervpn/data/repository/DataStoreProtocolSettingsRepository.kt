package com.wlvpn.consumervpn.data.repository

import androidx.datastore.core.DataStore
import com.wlvpn.consumervpn.data.IKEv2SettingsProto
import com.wlvpn.consumervpn.data.OpenVpnSettingsProto
import com.wlvpn.consumervpn.data.OpenVpnSettingsProto.InternetProtocolProto.TCP
import com.wlvpn.consumervpn.data.OpenVpnSettingsProto.InternetProtocolProto.UDP
import com.wlvpn.consumervpn.data.WireGuardSettingsProto
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.InternetProtocol
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort.Normal
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort.Scramble
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.Protocol.IKEv2
import com.wlvpn.consumervpn.domain.value.settings.Protocol.OpenVpn
import com.wlvpn.consumervpn.domain.value.settings.Protocol.WireGuard
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class DataStoreProtocolSettingsRepository(
    private val openVpnProtocolSettingsProto: DataStore<OpenVpnSettingsProto>,
    private val wireGuardProtocolSettingsProto: DataStore<WireGuardSettingsProto>,
    private val ikev2ProtocolSettingsProto: DataStore<IKEv2SettingsProto>,
) : ProtocolSettingsRepository {

    override fun getSettingsByProtocol(protocol: Protocol): Flow<ProtocolSettings> = flow {
        when (protocol) {

            OpenVpn -> emit(openVpnProtocolSettingsProto.data.map {
                ProtocolSettings.OpenVpn(
                    internetProtocol = when (it.internetProtocol) {
                        UDP -> InternetProtocol.UDP
                        TCP -> InternetProtocol.TCP
                        else -> InternetProtocol.TCP
                    },
                    scramble = it.scramble,
                    // Return the selected port
                    port = when {
                        it.scramble -> Scramble(value = it.scramblePort)
                        else -> Normal(value = it.port)
                    },
                    autoReconnect = it.autoReconnect,
                    allowLan = it.allowLan,
                    overrideMtu = it.overrideMtu
                )
            }.first())

            IKEv2 -> emit(
                ikev2ProtocolSettingsProto.data.map {
                    ProtocolSettings.IKEv2(
                        allowLan = it.allowLan
                    )
                }.first()
            )

            WireGuard -> emit(
                wireGuardProtocolSettingsProto.data.map {
                    ProtocolSettings.Wireguard(
                        allowLan = it.allowLan
                    )
                }.first()
            )
        }
    }

    override fun saveProtocolSettings(protocolSettings: ProtocolSettings): Flow<Unit> = flow {
        when (protocolSettings) {
            is ProtocolSettings.OpenVpn ->
                openVpnProtocolSettingsProto.updateData {
                    it.toBuilder().apply {
                        scramble = protocolSettings.scramble
                        internetProtocol = when (protocolSettings.internetProtocol) {
                            InternetProtocol.TCP -> OpenVpnSettingsProto.InternetProtocolProto.TCP
                            InternetProtocol.UDP -> OpenVpnSettingsProto.InternetProtocolProto.UDP
                        }

                        // Only save the type of port that was modified otherwise use the old value
                        when (val port = protocolSettings.port) {
                            is OpenVpnPort.Normal -> {
                                this.port = port.value
                                scramblePort = it.scramblePort
                            }

                            is OpenVpnPort.Scramble -> {
                                scramblePort = port.value
                                this.port = it.port
                            }
                        }

                        autoReconnect = protocolSettings.autoReconnect
                        allowLan = protocolSettings.allowLan
                        overrideMtu = protocolSettings.overrideMtu
                    }.build()
                }

            is ProtocolSettings.IKEv2 ->
                ikev2ProtocolSettingsProto.updateData {
                    it.toBuilder()
                        .setAllowLan(protocolSettings.allowLan)
                        .build()
                }

            is ProtocolSettings.Wireguard ->
                wireGuardProtocolSettingsProto.updateData {
                    it.toBuilder()
                        .setAllowLan(protocolSettings.allowLan)
                        .build()
                }
        }
        emit(Unit)
    }

    override fun clearProtocolSettings(): Flow<Unit> = flow {
        openVpnProtocolSettingsProto.updateData {
            it.toBuilder().clear().build()
        }

        ikev2ProtocolSettingsProto.updateData {
            it.toBuilder().clear().build()
        }

        wireGuardProtocolSettingsProto.updateData {
            it.toBuilder().clear().build()
        }

        emit(Unit)
    }
}