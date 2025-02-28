package com.wlvpn.consumervpn.domain.value

sealed class ConnectionTarget {
    data class Country(
        val code: String = ""
    ) : ConnectionTarget()

    data class City(
        val country: Country = Country(),
        val name: String = ""
    ) : ConnectionTarget()

    data class Server(
        val city: City = City(),
        val name: String = "",
    ) : ConnectionTarget()

    object Fastest : ConnectionTarget()
}
