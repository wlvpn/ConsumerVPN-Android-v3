package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow

interface SaveProtocolSettingsContract {

    interface Interactor {

        fun execute(
            protocolSettings: ProtocolSettings
        ): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        object UnableToCompleteFailure : Status()
    }
}