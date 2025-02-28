package com.wlvpn.consumervpn.data.gateway

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.domain.gateway.LoginGateway.InternalServerFailure
import com.wlvpn.consumervpn.domain.gateway.LoginGateway.UnableToMigrateUserSession
import com.wlvpn.consumervpn.domain.value.UserCredentials
import com.wlvpn.vpnsdk.domain.value.UserSession
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.GetUserSessionResponse
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.ImportLegacyUserDataResponse
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.ImportLegacyUserDataResponse.ImportNotNeeded
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.ImportLegacyUserDataResponse.LoginWithLegacyCredentialsFailure
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.ImportLegacyUserDataResponse.NoLegacyDataFound
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.ImportLegacyUserDataResponse.SuccessfulImport
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.ImportLegacyUserDataResponse.UnableToImportLegacyDataFailure
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.EmptyPassword
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.EmptyUsername
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.InvalidCredentials
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.NotConnected
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.ServiceError
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.Success
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.TooManyAttempts
import com.wlvpn.vpnsdk.sdk.fetures.account.VpnAccount.LoginResponse.UnableToLogin
import com.wlvpn.vpnsdk.sdk.value.LoginRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class VpnSdkLoginGateway(
    private val vpnAccount: VpnAccount
) : LoginGateway {

    override fun login(userCredentials: UserCredentials): Flow<Unit> =
        vpnAccount.login(
            LoginRequest.WithCredentials(
                username = userCredentials.username,
                password = userCredentials.password
            )
        )
            .map {
                when (it) {
                    Success -> Unit
                    EmptyUsername -> throw LoginGateway.EmptyUsernameFailure()
                    EmptyPassword -> throw LoginGateway.EmptyPasswordFailure()
                    InvalidCredentials -> throw LoginGateway.InvalidCredentialsFailure()

                    NotConnected -> throw LoginGateway.ConnectionFailure()

                    // These are not required to be parsed with credentials auth, send
                    // NotAuthorizedFailure if triggered
                    VpnAccount.LoginResponse.EmptyAccessToken,
                    VpnAccount.LoginResponse.EmptyRefreshToken,
                    VpnAccount.LoginResponse.InvalidAccessToken,
                    VpnAccount.LoginResponse.ExpiredAccessToken,
                    VpnAccount.LoginResponse.ExpiredRefreshToken,
                    VpnAccount.LoginResponse.InvalidApiKey,
                    VpnAccount.LoginResponse.UnableToRefreshToken,
                    VpnAccount.LoginResponse.InvalidVpnSdkApiConfig ->
                        throw LoginGateway.NotAuthorizedFailure()

                    TooManyAttempts -> throw LoginGateway.TooManyRequests()

                    is UnableToLogin -> throw LoginGateway.UnexpectedFailure(
                        message = it.throwable?.javaClass?.canonicalName ?: ""
                    )

                    is ServiceError -> throw LoginGateway.InternalServerFailure(
                        message = it.reason ?: "",
                        responseCode = it.code
                    )
                }
            }
            .catch {
                throw if (it !is Failure) {
                    LoginGateway.UnexpectedFailure(
                        message = it.javaClass.canonicalName ?: "", throwable = it
                    )
                } else {
                    it
                }
            }

    override fun logout(): Flow<Unit> =
        vpnAccount.logout().map {
            when (it) {
                VpnAccount.LogoutResponse.ExpiredAccessToken ->
                    throw LoginGateway.ExpiredAccessTokenFailure()
                VpnAccount.LogoutResponse.InvalidAccessToken ->
                    throw LoginGateway.InvalidAccessTokenFailure()
                VpnAccount.LogoutResponse.Success -> Unit
                is VpnAccount.LogoutResponse.UnableToLogout ->
                    throw LoginGateway.UnableToLogoutFailure()
            }
        }.catch { throw it }

    override fun isLoggedIn(): Flow<Boolean> =
        vpnAccount.getUserSession()
            .map {
                it is GetUserSessionResponse.Success && it.userSession is UserSession.Active
            }

    override fun migrateUserSession(): Flow<Boolean> = vpnAccount.importLegacyUserData()
        .map {
            when (it) {
                SuccessfulImport -> true

                NoLegacyDataFound,
                ImportNotNeeded -> false

                is LoginWithLegacyCredentialsFailure ->
                    throw UnableToMigrateUserSession(message = it.message)

                is ImportLegacyUserDataResponse.ServiceError ->
                    throw InternalServerFailure(it.code, it.reason ?: "")

                is UnableToImportLegacyDataFailure ->
                    throw UnableToMigrateUserSession(throwable = it.throwable)
            }
        }
}