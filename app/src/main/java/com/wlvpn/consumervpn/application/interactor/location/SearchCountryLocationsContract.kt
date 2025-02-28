package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface SearchCountryLocationsContract {

    interface Interactor {

        fun execute(term: String): Flow<Status>
    }

    sealed class Status {

        class SearchResults(
            val locationList: List<ServerLocation.Country>,
            val savedTarget: ConnectionTarget
        ) : Status()

        object NoSearchResults : Status()

        object EmptySearchTerm : Status()

        object UnableToRetrieveLocationsFailure : Status()
    }

    sealed class Event {
        data class SearchEvent(val searchTerm: String) : Event()
    }
}