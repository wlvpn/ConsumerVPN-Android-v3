package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.application.interactor.location.RetrieveCountryLocationsContract.Status
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class RetrieveCountryLocationsInteractor(
    private val serverGateway: ExternalServersGateway,
    private val connectionSettingsRepository: ConnectionSettingsRepository
    ) : RetrieveCountryLocationsContract.Interactor {

    override fun execute(): Flow<Status> =
        combine(
            serverGateway.retrieveCountryLocations(),
            serverGateway.retrieveCityLocations(),
            connectionSettingsRepository.getConnectionSettings()
        ) { countries, cities, settings ->
                Status.Success(
                    countryLocationsList = countries.map {  country ->
                        country.copy(cities = cities.filter {
                            city -> city.country.code == country.code
                        })
                    }, savedTarget = settings.selectedTarget
                ) as Status
            }.catch { throwable ->
                Timber.e(throwable)
                when (throwable) {
                    is ExternalServersGateway.UnableToRetrieveCountryServersLocationsFailure ->
                        emit(Status.UnableToRetrieveCountryLocationsFailure)
                    else -> throw  throwable
                }
            }
}