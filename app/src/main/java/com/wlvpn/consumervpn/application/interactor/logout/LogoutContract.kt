package com.wlvpn.consumervpn.application.interactor.logout

import kotlinx.coroutines.flow.Flow

interface LogoutContract {
    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        object Success : Status()
        object ExpiredAccessToken : Status()
        object InvalidAccessToken : Status()
        object UnableToLogout : Status()
    }
}