package com.wlvpn.consumervpn.application.interactor.login

import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserContract.Status
import com.wlvpn.consumervpn.domain.gateway.LoginGateway
import com.wlvpn.consumervpn.util.catchOrEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MigrateLegacyUserInteractor(
    private val loginGateway: LoginGateway
) : MigrateLegacyUserContract.Interactor {

    override fun execute(): Flow<Status> =
        loginGateway.migrateUserSession()
            .map { hasMigratedUser ->
                if (hasMigratedUser) {
                    Status.MigrationSuccess
                } else {
                    Status.NoMigrationNeeded
                }
            }.catchOrEmpty {
                Status.UnableToMigrateFailure(it)
            }
}