package com.wlvpn.consumervpn.domain.value

import java.io.Serializable

sealed class ServerLocation : Serializable {

    data class Country(
        val name: String = "",
        val code: String = "",
        val cities: List<City> = emptyList(),
        val searchedBy: SearchMatchType? = null
    ) : ServerLocation()

    data class City(
        val country: Country = Country(),
        val name: String = "",
    ) : ServerLocation()

    data class Server(
        val city: City = City(),
        val name: String = "",
        val load: Int = 0,
    ) : ServerLocation()

    object Fastest : ServerLocation()
}