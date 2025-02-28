package com.wlvpn.consumervpn.presentation.settings

import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings

sealed class SettingsEvent {
    object LoadingDataEvent : SettingsEvent()
    object ErrorGettingSettings : SettingsEvent()
    object ErrorPreparingThreatProtection : SettingsEvent()

    object SuccessLogout : SettingsEvent()
    object ExpiredAccessToken : SettingsEvent()
    object InvalidAccessToken : SettingsEvent()
    object UnableToLogout : SettingsEvent()

    data class SettingsReceived(
        val connectionSettings: ConnectionSettings,
        val protocolSettings: ProtocolSettings,
        val availableVpnPorts: List<Int>
    ) : SettingsEvent()
}