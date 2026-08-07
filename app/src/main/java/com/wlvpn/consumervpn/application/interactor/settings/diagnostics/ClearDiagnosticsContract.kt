package com.wlvpn.consumervpn.application.interactor.settings.diagnostics

import kotlinx.coroutines.flow.Flow

interface ClearDiagnosticsContract {
    interface Interactor {
        fun execute(): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        data class UnableToClearFailure(val throwable: Throwable? = null) : Status()
    }
}