package com.wlvpn.consumervpn.domain.gateway

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface ExternalServersGateway {

    fun retrieveCountryLocations(): Flow<List<ServerLocation.Country>>

    fun retrieveCityLocations(): Flow<List<ServerLocation.City>>

    // Failures
    class UnableToRetrieveCountryServersLocationsFailure : Failure(
        "Unable to retrieve country locations"
    )

    class UnableToRetrieveCityServerLocationsFailure : Failure(
        message = "Unable to retrieve server locations by city"
    )

    class UpdateServersFailure(throwable: Throwable? = null, message: String? = null) : Failure(
        message = message ?: "Error updating servers",
        throwable = throwable
    )

    class UpdateProtocolsFailure(throwable: Throwable? = null, message: String? = null) : Failure(
        message = message ?: "Error updating protocols",
        throwable = throwable
    )

    fun updateServers(): Flow<Unit>
}