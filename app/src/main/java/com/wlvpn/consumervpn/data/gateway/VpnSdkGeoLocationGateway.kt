package com.wlvpn.consumervpn.data.gateway

import com.wlvpn.consumervpn.domain.gateway.GeoLocationGateway
import com.wlvpn.consumervpn.domain.value.GeoLocation
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.GeoLocationResponse.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VpnSdkGeoLocationGateway(
    private val vpnConnection: VpnConnection
) : GeoLocationGateway {

    override fun fetchGeoLocation(): Flow<GeoLocation> =
        vpnConnection.fetchGeoLocation()
            .map {
                when (it) {
                    is Success -> GeoLocation(
                        ip = it.geoInfo.ip,
                        countryCode = it.geoInfo.countryCode,
                        cityName = it.geoInfo.city,
                        lat = it.geoInfo.latitude,
                        lon = it.geoInfo.longitude
                    )

                    else -> throw GeoLocationGateway.IncompleteGeoLocationDataFailure()
                }
            }
}