package com.wlvpn.consumervpn.presentation.home

import com.wlvpn.consumervpn.domain.value.GeoLocation

sealed class GeoLocationEvent {
    data class GeoLocationChanged(val geoLocation: GeoLocation) : GeoLocationEvent()
    object Error : GeoLocationEvent()
}