package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.domain.value.GeoLocation
import kotlinx.coroutines.flow.Flow

interface FetchGeoLocationContract {

    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {
        data class Success(val geolocation: GeoLocation) : Status()
        data class UnableToFetchGeoLocationFailure(val message: String?) : Status()
    }
}