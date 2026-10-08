package com.wlvpn.consumervpn.domain.repository

import com.wlvpn.consumervpn.domain.value.DeviceInformation
import kotlinx.coroutines.flow.Flow

interface DeviceAndBuildInfoRepository {
    fun getDeviceInformation(): Flow<DeviceInformation>
}
