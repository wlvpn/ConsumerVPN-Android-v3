package com.wlvpn.consumervpn.application.interactor

import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.value.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetUserSessionInteractor(
    private val loginGateway: LoginGateway
) : GetUserSessionContract.Interactor {
    override fun execute(): Flow<GetUserSessionContract.Status> =
        loginGateway.isLoggedIn()
            .map {
                GetUserSessionContract.Status.Success(UserSession(isLoggedIn = it))
            }
            .catch { GetUserSessionContract.Status.UnableToGetUserSessionFailure(it) }

}