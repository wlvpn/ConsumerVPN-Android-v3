package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.application.interactor.connectivity.FetchGeoLocationContract.Status
import com.wlvpn.consumervpn.domain.gateway.GeoLocationGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class FetchGeoLocationInteractor(
    private val geoLocationGateway: GeoLocationGateway
) : FetchGeoLocationContract.Interactor {

    override fun execute(): Flow<Status> =
        geoLocationGateway.fetchGeoLocation()
            .map {
                Status.Success(it) as Status
            }
            .catch {
                emit(Status.UnableToFetchGeoLocationFailure(it.localizedMessage))
            }
}