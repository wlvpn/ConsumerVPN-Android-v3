package com.wlvpn.consumervpn.application.interactor.settings.diagnostics

import com.wlvpn.consumervpn.application.interactor.settings.diagnostics.ClearDiagnosticsContract.Status
import com.wlvpn.consumervpn.domain.gateway.DiagnosticsGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ClearDiagnosticsInteractor @Inject constructor(
    private val diagnosticsGateway: DiagnosticsGateway
) : ClearDiagnosticsContract.Interactor {

    override fun execute(): Flow<Status> = diagnosticsGateway.clearDiagnostics()
        .map { Status.Success as Status }
        .catch { throwable ->
            emit(Status.UnableToClearFailure(throwable))
        }
}