package com.wlvpn.consumervpn.application.interactor

import com.wlvpn.consumervpn.domain.value.UserSession
import kotlinx.coroutines.flow.Flow

interface GetUserSessionContract {

    interface Interactor {
        fun execute(): Flow<Status>
    }

    sealed class Status {
        data class Success(val userSession: UserSession) : Status()
        data class UnableToGetUserSessionFailure(val throwable: Throwable? = null) : Status()
    }
}