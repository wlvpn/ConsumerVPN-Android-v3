package com.wlvpn.consumervpn.domain.value

sealed class SearchMatchType {
    object CountryName : SearchMatchType()
    object CityName : SearchMatchType()
}
