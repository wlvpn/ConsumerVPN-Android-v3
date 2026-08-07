package com.wlvpn.consumervpn.application.interactor.settings.splittunneling

import com.wlvpn.consumervpn.application.interactor.settings.splittunneling.RetrieveInstalledAppsContract.Status
import com.wlvpn.consumervpn.domain.gateway.SplitTunnelGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RetrieveInstalledAppsInteractor @Inject constructor(
    private val splitTunnelGateway: SplitTunnelGateway
) : RetrieveInstalledAppsContract.Interactor {

    override fun execute(): Flow<Status> = flow<Status> {
        emit(Status.Success(splitTunnelGateway.getInstalledApps()))
    }.catch { throwable ->
        emit(Status.UnableToRetrieveAppsFailure(throwable))
    }
}