package com.wlvpn.consumervpn.application.interactor.login

import com.wlvpn.consumervpn.application.interactor.login.LoginContract.Status
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.value.UserCredentials
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

class LoginInteractor(
    private val loginGateway: LoginGateway,
    private val externalServersGateway: ExternalServersGateway,
    ) : LoginContract.Interactor {

    @OptIn(FlowPreview::class)
    override fun execute(credentials: UserCredentials): Flow<Status> =
        loginGateway.login(UserCredentials(credentials.username, credentials.password))
            .flatMapConcat {
            externalServersGateway.updateServers()
        }.map {
                Status.Success as Status
            }.catch { throwable ->
                val error = when (throwable) {
                    is LoginGateway.EmptyUsernameFailure -> Status.EmptyUsernameFailure
                    is LoginGateway.EmptyPasswordFailure -> Status.EmptyPasswordFailure

                    is LoginGateway.InvalidCredentialsFailure ->
                        Status.InvalidCredentialsFailure

                    is LoginGateway.TooManyRequests ->
                        Status.TooManyRequestsFailure

                    is LoginGateway.ConnectionFailure ->
                        Status.ConnectionFailure

                    is LoginGateway.UnexpectedFailure ->
                        Status.UnableToLoginFailure(
                            message = throwable.message ?: "",
                            errorCode = throwable.responseCode
                        )

                    is LoginGateway.InternalServerFailure ->
                        Status.UnableToLoginFailure(
                            message = throwable.message ?: "",
                            errorCode = throwable.responseCode
                        )

                    is LoginGateway.NotAuthorizedFailure ->
                        Status.NotAuthorizedFailure

                    else ->
                        Status.UnableToLoginFailure(message = throwable.message ?: "")
                }
                emit(error)
            }
}
