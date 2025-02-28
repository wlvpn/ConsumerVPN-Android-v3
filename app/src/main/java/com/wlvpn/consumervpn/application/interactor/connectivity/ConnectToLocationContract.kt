package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface ConnectToLocationContract {
   interface Interactor {

        fun execute(location: ServerLocation): Flow<Status>
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