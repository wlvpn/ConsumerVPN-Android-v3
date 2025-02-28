package com.wlvpn.consumervpn.application.interactor.settings

import kotlinx.coroutines.flow.Flow

class PrepareThreatProtectionContract {
    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        data object Success : Status()
        data object NotAuthorizedFailure : Status()
        data class UnableToPrepareThreatProtectionFailure(
            val throwable: Throwable? = null
        ) : Status()
    }
}