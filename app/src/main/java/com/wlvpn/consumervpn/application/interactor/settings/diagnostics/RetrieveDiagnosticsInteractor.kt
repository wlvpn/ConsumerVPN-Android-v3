package com.wlvpn.consumervpn.application.interactor.settings.diagnostics

import com.wlvpn.consumervpn.application.interactor.settings.diagnostics.RetrieveDiagnosticsContract.Status
import com.wlvpn.consumervpn.domain.gateway.DiagnosticsGateway
import com.wlvpn.consumervpn.domain.repository.DeviceAndBuildInfoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class RetrieveDiagnosticsInteractor @Inject constructor(
    private val diagnosticsGateway: DiagnosticsGateway,
    private val deviceAndBuildInfoRepository: DeviceAndBuildInfoRepository
) : RetrieveDiagnosticsContract.Interactor {

    override fun execute(): Flow<Status> = deviceAndBuildInfoRepository.getDeviceInformation()
        .flatMapConcat { deviceInformation ->
            diagnosticsGateway.getDiagnostics()
                .map { diagnostics ->
                    Status.Success(
                        deviceInformation = deviceInformation,
                        diagnostics = diagnostics
                    ) as Status
                }
        }.catch { throwable ->
            emit(Status.UnableToRetrieveDiagnosticsFailure(throwable))
        }
}