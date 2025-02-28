package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import kotlinx.coroutines.flow.Flow

interface SaveConnectionSettingsContract {

    interface Interactor {

        fun execute(
            connectionsSettings: ConnectionSettings
        ): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        object UnableToSaveFailure : Status()
    }
}