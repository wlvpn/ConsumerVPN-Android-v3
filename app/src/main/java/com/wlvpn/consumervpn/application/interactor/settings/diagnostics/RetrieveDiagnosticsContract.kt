package com.wlvpn.consumervpn.application.interactor.settings.diagnostics

import com.wlvpn.consumervpn.domain.value.DeviceInformation
import kotlinx.coroutines.flow.Flow

interface RetrieveDiagnosticsContract {
    interface Interactor {
        fun execute(): Flow<Status>
    }

    sealed class Status {
        data class Success(
            val deviceInformation: DeviceInformation,
            val diagnostics: List<String>
        ) : Status()
        data class UnableToRetrieveDiagnosticsFailure(val throwable: Throwable? = null) : Status()
    }
}