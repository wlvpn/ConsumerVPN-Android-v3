package com.wlvpn.consumervpn.data.repository

import android.os.Build
import com.wlvpn.consumervpn.BuildConfig
import com.wlvpn.consumervpn.domain.repository.DeviceAndBuildInfoRepository
import com.wlvpn.consumervpn.domain.value.DeviceInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DefaultDeviceAndBuildInfoRepository : DeviceAndBuildInfoRepository {

    override fun getDeviceInformation(): Flow<DeviceInformation> = flowOf(
        DeviceInformation(
            device = Build.DEVICE,
            brand = Build.MANUFACTURER,
            osVersion = Build.VERSION.SDK_INT,
            vpnSdkVersion = BuildConfig.VPN_SDK_VERSION,
            appVersion = BuildConfig.VERSION_NAME
        )
    )
}
