package com.wlvpn.consumervpn.domain.repository

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow

interface ProtocolSettingsRepository {

    fun getSettingsByProtocol(protocol: Protocol): Flow<ProtocolSettings>

    fun saveProtocolSettings(protocolSettings: ProtocolSettings): Flow<Unit>

    fun clearProtocolSettings(): Flow<Unit>

    class UnableToSaveProtocolSettingsFailure(message: String) : Failure(message)
    class UnableToClearProtocolSettingsFailure(message: String) : Failure(message)
}