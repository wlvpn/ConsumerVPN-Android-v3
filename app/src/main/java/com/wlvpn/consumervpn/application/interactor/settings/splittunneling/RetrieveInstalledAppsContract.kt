package com.wlvpn.consumervpn.application.interactor.settings.splittunneling

import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings
import kotlinx.coroutines.flow.Flow

interface RetrieveInstalledAppsContract {

    interface Interactor {
        fun execute(): Flow<Status>
    }

    sealed class Status {
        data class Success(val apps: List<SplitTunnelSettings.App>) : Status()
        data class UnableToRetrieveAppsFailure(val throwable: Throwable? = null) : Status()
    }
}