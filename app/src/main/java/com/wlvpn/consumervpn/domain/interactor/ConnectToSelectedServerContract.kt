package com.wlvpn.consumervpn.domain.interactor

import kotlinx.coroutines.flow.Flow

interface ConnectToSelectedServerContract {

    interface DomainInteractor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        object UserNotLoggedFailure : Status()
        object NoNetworkFailure : Status()
        object VpnNotPreparedFailure : Status()
        data class UnableToConnectFailure(val reason: String?) : Status()
        object InvalidatedAccountFailure : Status()
        object ExpiredAccountFailure : Status()
        data class VpnServiceFailure(val errorCode: String?) : Status()
    }
}
