package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine

class SearchCityLocationsInteractor(
    private val serverGateway: ExternalServersGateway,
    private val connectionSettingsRepository: ConnectionSettingsRepository,
    ) : SearchCityLocationsContract.Interactor {

    override fun execute(term: String): Flow<SearchCityLocationsContract.Status> = channelFlow {
        val trimTerm = term.trim()

        if (trimTerm.isNotEmpty()) {

            combine(
                serverGateway.retrieveCountryLocations(),
                serverGateway.retrieveCityLocations(),
                connectionSettingsRepository.getConnectionSettings()
            ) { countries, cities, settings ->
                // We include the countries on search too
                val resultList = cities.filter { cityLocation ->
                    cityLocation.name.contains(trimTerm, true) ||
                        countries.any { country ->
                            country.code == cityLocation.country.code &&
                            country.name.contains(trimTerm, ignoreCase = true)
                        }
                }.sortedBy { it.name }

                if (resultList.isNotEmpty()) {
                    SearchCityLocationsContract.Status.SearchResults(
                        resultList,
                        settings.selectedTarget
                    )
                } else {
                    SearchCityLocationsContract.Status.NoSearchResults
                }
            }.collect {
                    send(it as SearchCityLocationsContract.Status)
                }
        } else {
            send(SearchCityLocationsContract.Status.EmptySearchTerm)
        }
    }.catch { throwable ->
                when (throwable) {
                    is ExternalServersGateway.UnableToRetrieveCountryServersLocationsFailure,
                    is ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure -> emit(
                        SearchCityLocationsContract.Status.UnableToRetrieveLocationsFailure
                    )
                    else -> throw throwable
                }
            }
}