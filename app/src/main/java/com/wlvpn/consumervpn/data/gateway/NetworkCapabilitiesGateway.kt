package com.wlvpn.consumervpn.data.gateway

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.wlvpn.consumervpn.domain.gateway.NetworkGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NetworkCapabilitiesGateway(
    private val connectivityManager: ConnectivityManager
) : NetworkGateway {

    private val supportedCapabilities = listOf(
        NetworkCapabilities.TRANSPORT_CELLULAR,
        NetworkCapabilities.TRANSPORT_WIFI,
        NetworkCapabilities.TRANSPORT_ETHERNET,
        NetworkCapabilities.TRANSPORT_BLUETOOTH
    )

    override fun isNetworkAvailable(): Flow<Boolean> = flow {
        connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.let { actualCapabilities ->
                // We iterate through the list of capabilities supported and reduce them
                // to a single boolean to detect if we have network or not
                val result = supportedCapabilities.map {
                    actualCapabilities.hasTransport(it)
                }.firstOrNull { it } ?: false
                emit(result)
            } ?: emit(false)
        } ?: emit(false)
    }
}