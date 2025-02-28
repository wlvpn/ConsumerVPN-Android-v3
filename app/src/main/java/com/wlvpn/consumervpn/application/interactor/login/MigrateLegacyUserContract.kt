package com.wlvpn.consumervpn.application.interactor.login

import kotlinx.coroutines.flow.Flow

interface MigrateLegacyUserContract {

    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        object MigrationSuccess : Status()
        object NoMigrationNeeded : Status()
        data class UnableToMigrateFailure(
            val throwable: Throwable?
        ) : Status()
    }
}