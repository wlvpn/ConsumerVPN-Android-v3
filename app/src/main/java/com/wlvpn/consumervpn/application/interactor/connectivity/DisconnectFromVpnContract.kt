package com.wlvpn.consumervpn.application.interactor.connectivity

import kotlinx.coroutines.flow.Flow

interface DisconnectFromVpnContract {

    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        data class UnableDisconnectFailure(val reason: String?) : Status()
    }
}