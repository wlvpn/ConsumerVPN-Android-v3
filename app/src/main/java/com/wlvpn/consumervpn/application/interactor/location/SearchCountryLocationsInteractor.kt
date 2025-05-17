package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract.Status
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.SearchMatchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine

class SearchCountryLocationsInteractor(
    private val serverGateway: ExternalServersGateway,
    private val connectionSettingsRepository: ConnectionSettingsRepository
) : SearchCountryLocationsContract.Interactor {

    override fun execute(term: String): Flow<Status> = channelFlow {
                val trimTerm = term.trim()
                if (trimTerm.isNotEmpty()) {

                    combine(
                        serverGateway.retrieveCountryLocations(),
                        serverGateway.retrieveCityLocations(),
                        connectionSettingsRepository.getConnectionSettings()
                    ) { countries, cities, settings ->
                            // We include the cities on search too
                            val resultList =  countries.filter { countryLocation ->
                                countryLocation.name.contains(trimTerm, true) ||
                                    cities.any { city ->
                                        city.country.code == countryLocation.code &&
                                            city.name.contains(
                                                trimTerm,
                                                ignoreCase = true
                                            )
                                    }
                            }.map { countryLocation ->
                                val isCountryMatch = countryLocation.name.contains(
                                    trimTerm, ignoreCase = true
                                )
                                countryLocation.copy(
                                    searchedBy = if (isCountryMatch) {
                                        SearchMatchType.CountryName
                                    } else {
                                        SearchMatchType.CityName
                                    }
                                )
                            }.map {  country ->
                                country.copy(cities = cities.filter {
                                        city -> city.country.code == country.code
                                })
                            }
                            if (resultList.isNotEmpty()) {
                                    Status.SearchResults(
                                        resultList, settings.selectedTarget
                                    )
                            } else {
                                Status.NoSearchResults
                            }
                        }.collect {
                            send(it as Status)
                        }
                } else {
                    send(Status.EmptySearchTerm)
                }
            }.catch { throwable ->
                when (throwable) {
                    is ExternalServersGateway.UnableToRetrieveCountryServersLocationsFailure,
                    is ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure ->
                        emit(Status.UnableToRetrieveLocationsFailure)
                    else -> throw throwable
                }
            }
}