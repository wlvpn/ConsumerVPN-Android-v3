package com.wlvpn.consumervpn.application.interactor.logout

import com.wlvpn.consumervpn.application.interactor.logout.LogoutContract.Status
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class LogoutInteractor(
    private val loginGateway: LoginGateway
) : LogoutContract.Interactor {

    @OptIn(FlowPreview::class)
    override fun execute(): Flow<Status> =
        loginGateway.logout()
            .map {
                Status.Success as Status
            }.catch { throwable ->
                val error = when (throwable) {
                    is LoginGateway.ExpiredAccessTokenFailure -> Status.ExpiredAccessToken
                    is LoginGateway.InvalidAccessTokenFailure -> Status.InvalidAccessToken
                    is LoginGateway.UnableToLogoutFailure -> Status.UnableToLogout
                    else -> { Status.UnableToLogout }
                }
                emit(error)
            }
}