package com.wlvpn.consumervpn.application.interactor.settings.diagnostics

import com.wlvpn.consumervpn.application.interactor.settings.diagnostics.RetrieveDiagnosticsPathContract.Status
import com.wlvpn.consumervpn.domain.gateway.DiagnosticsGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RetrieveDiagnosticsPathInteractor @Inject constructor(
    private val diagnosticsGateway: DiagnosticsGateway
) : RetrieveDiagnosticsPathContract.Interactor {

    override fun execute(): Flow<Status> = diagnosticsGateway.getDiagnosticsPath()
        .map { path -> Status.Success(path) as Status }
        .catch { throwable ->
            emit(Status.UnableToRetrievePathFailure(throwable))
        }
}