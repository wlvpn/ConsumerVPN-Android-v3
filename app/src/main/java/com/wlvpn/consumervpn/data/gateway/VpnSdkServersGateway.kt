package com.wlvpn.consumervpn.data.gateway

import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.FindCountriesResponse.NoCountriesFound
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.FindCountriesResponse.Success
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.FindCountriesResponse.UnableToFindCountries
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@FlowPreview
class VpnSdkServersGateway(
    private val vpnConnection: VpnConnection
) : ExternalServersGateway {

    override fun retrieveCountryLocations(): Flow<List<ServerLocation.Country>> =
        vpnConnection.findCountries()
            .map { response ->
                when (response) {
                    is UnableToFindCountries,
                    NoCountriesFound ->
                    throw ExternalServersGateway.UnableToRetrieveCountryServersLocationsFailure()
                    is Success ->
                        response.countries.sortedBy { it.name }
                            .map { ServerLocation.Country(name = it.name, code = it.code) }
                }
            }

    override fun retrieveCityLocations(): Flow<List<ServerLocation.City>> =
        vpnConnection.findCities()
            .map { response ->
                when (response) {
                    is VpnConnection.FindCitiesResponse.UnableToFindCities,
                    VpnConnection.FindCitiesResponse.NoCitiesFound ->
                        throw ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure()

                    is VpnConnection.FindCitiesResponse.Success ->
                        response.cities
                            .sortedBy { it.country.name }.map {
                                val foundCity = it
                                val country = ServerLocation.Country(
                                    name = foundCity.country.name,
                                    code = foundCity.country.code
                                )

                                ServerLocation.City(country = country, name = foundCity.name)
                            }
                }
            }

    override fun updateServers(): Flow<Unit> =
        vpnConnection.updateServers()
            .map {

                when (it) {
                    VpnConnection.UpdateServersResponse.Success ->
                        Unit

                    is VpnConnection.UpdateServersResponse.ServiceError ->
                        throw
                            ExternalServersGateway.UpdateServersFailure(
                                message = "Service Error: ${it.reason}, code = ${it.code}"
                            )

                    is VpnConnection.UpdateServersResponse.UnableToUpdateServers ->
                        throw ExternalServersGateway.UpdateServersFailure(it.throwable)

                    else -> throw ExternalServersGateway.UpdateServersFailure(
                        message = it::class.simpleName
                    )
                }
            }
}