package com.wlvpn.consumervpn.domain.repository

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import kotlinx.coroutines.flow.Flow

interface ConnectionSettingsRepository {

    fun getConnectionSettings(): Flow<ConnectionSettings>

    fun saveConnectionSettings(connectionSettings: ConnectionSettings): Flow<Unit>

    fun clearConnectionSettings(): Flow<Unit>

    class UnableToSaveConnectionSettingsFailure(message: String) : Failure(message)

    class UnableToClearConnectionSettingsFailure(message: String) : Failure(message)
}