package com.wlvpn.consumervpn.domain.gateway

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.UserCredentials
import kotlinx.coroutines.flow.Flow

interface LoginGateway {

    fun login(userCredentials: UserCredentials): Flow<Unit>

    fun logout(): Flow<Unit>

    fun isLoggedIn(): Flow<Boolean>

    fun migrateUserSession(): Flow<Boolean>

    // Failures
    class ConnectionFailure : Failure()
    class EmptyUsernameFailure : Failure()
    class EmptyPasswordFailure : Failure()
    class InvalidCredentialsFailure : Failure()
    class NotAuthorizedFailure : Failure()
    class ExpiredAccessTokenFailure : Failure()
    class InvalidAccessTokenFailure : Failure()
    class UnableToLogoutFailure : Failure()
    class InternalServerFailure(
        val responseCode: Int = 0,
        message: String
    ) : Failure(message)

    class TooManyRequests : Failure()
    class UnexpectedFailure(
        val responseCode: Int = 0,
        message: String,
        throwable: Throwable? = null
    ) : Failure(message, throwable)

    class UnableToMigrateUserSession(
        throwable: Throwable? = null,
        message: String = ""
    ) : Failure(message, throwable)
}