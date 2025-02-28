package com.wlvpn.consumervpn.application.interactor.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectOnBootContract {

    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        object NoSettingsFoundFailure : Status()
        object SaveConnectionSettingsFailure : Status()
        object NoNetworkFailure : Status()
        object UnableToConnectFailure : Status()
        object AuthenticationFailure : Status()
        object VpnNotPreparedFailure : Status()
        data class ServiceFailure(val code: String?) : Status()
    }
}