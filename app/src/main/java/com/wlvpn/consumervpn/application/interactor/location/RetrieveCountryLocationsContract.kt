package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface RetrieveCountryLocationsContract {
    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {

        class Success(
            val countryLocationsList: List<ServerLocation.Country>,
            val savedTarget: ConnectionTarget,
            ) : Status()

        object UnableToRetrieveCountryLocationsFailure : Status()

        object UnableToRetrieveSelectedServerFailure : Status()
    }
}