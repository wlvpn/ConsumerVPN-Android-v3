package com.wlvpn.consumervpn.application.interactor.login

import com.wlvpn.consumervpn.domain.value.UserCredentials
import kotlinx.coroutines.flow.Flow

interface LoginContract {

    interface Interactor {

        fun execute(credentials: UserCredentials): Flow<Status>
    }

    sealed class Status {
        object Success : Status()

        object EmptyUsernameFailure : Status()
        object EmptyPasswordFailure : Status()

        object InvalidCredentialsFailure : Status()
        object ConnectionFailure : Status()
        object NotAuthorizedFailure : Status()
        object TooManyRequestsFailure : Status()
        data class UnableToLoginFailure(
            val message: String = "",
            val errorCode: Int = 0
        ) : Status()
    }
}