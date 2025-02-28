package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface RetrieveCityLocationsContract {
    interface Interactor {
        fun execute(): Flow<Status>
    }

    sealed class Status {

        class Success(
            val cityLocationsList: List<ServerLocation.City>,
            val savedTarget: ConnectionTarget
            ) : Status()

        object UnableToRetrieveCityLocationsFailure : Status()
    }
}