package com.wlvpn.consumervpn.domain.gateway

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.GeoLocation
import kotlinx.coroutines.flow.Flow

interface GeoLocationGateway {

    fun fetchGeoLocation(): Flow<GeoLocation>

    class IncompleteGeoLocationDataFailure : Failure()
}