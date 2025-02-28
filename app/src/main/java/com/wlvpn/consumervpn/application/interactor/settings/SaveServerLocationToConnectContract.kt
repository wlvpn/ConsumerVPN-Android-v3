package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface SaveServerLocationToConnectContract {
    interface Interactor {

        fun execute(serverLocation: ServerLocation): Flow<Status>
    }

    sealed interface Status {
        data object Success : Status
        data class UnableToSaveServerLocation(val throws: Throwable? = null) : Status
    }
}