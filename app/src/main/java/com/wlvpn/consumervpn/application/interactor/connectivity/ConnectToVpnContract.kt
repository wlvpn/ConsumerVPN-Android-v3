package com.wlvpn.consumervpn.application.interactor.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectToVpnContract {
   interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        object UserNotLoggedFailure : Status()
        object NoNetworkFailure : Status()
        object VpnNotPreparedFailure : Status()
        data class UnableToConnectFailure(val reason: String? = null) : Status()
        data class InvalidWireGuardApiResponseFailure(val errorCode: String?) : Status()
        object InactiveWireGuardAccountFailure : Status()
        object ExpiredWireGuardAccountFailure : Status()
    }
}