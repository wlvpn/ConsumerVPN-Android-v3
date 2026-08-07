package com.wlvpn.consumervpn.application.interactor.settings.diagnostics

import kotlinx.coroutines.flow.Flow

interface RetrieveDiagnosticsPathContract {
    interface Interactor {
        fun execute(): Flow<Status>
    }

    sealed class Status {
        data class Success(val path: String) : Status()
        data class UnableToRetrievePathFailure(val throwable: Throwable? = null) : Status()
    }
}