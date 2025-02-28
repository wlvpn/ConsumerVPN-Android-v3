package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow

interface RetrieveAllConnectionSettingsContract {

    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {

        data class Success(
            val connectionSettings: ConnectionSettings,
            val protocolSettings: ProtocolSettings,
            val availableVpnPorts: List<Int>
        ) : Status()

        object UnableToRetrieveSettingsFailure : Status()
    }
}