package com.wlvpn.consumervpn.domain.value

data class DeviceInformation(
    val device: String,
    val brand: String,
    val osVersion: Int,
    val vpnSdkVersion: String,
    val appVersion: String
)
