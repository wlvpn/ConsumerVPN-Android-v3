package com.wlvpn.consumervpn.domain.gateway

import kotlinx.coroutines.flow.Flow

interface NetworkGateway {
    fun isNetworkAvailable(): Flow<Boolean>
}