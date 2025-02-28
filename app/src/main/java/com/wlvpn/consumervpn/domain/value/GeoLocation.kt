package com.wlvpn.consumervpn.domain.value

data class GeoLocation(
    val ip: String,
    val countryCode: String,
    val cityName: String?,
    val lat: Double,
    val lon: Double
)