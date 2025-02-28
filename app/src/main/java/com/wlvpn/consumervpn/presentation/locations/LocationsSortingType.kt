package com.wlvpn.consumervpn.presentation.locations

sealed class LocationsSortingType {

    object ByCountry : LocationsSortingType()

    object ByCity : LocationsSortingType()

}