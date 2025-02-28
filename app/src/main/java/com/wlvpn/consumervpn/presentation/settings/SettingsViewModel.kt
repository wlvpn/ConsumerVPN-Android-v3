package com.wlvpn.consumervpn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.logout.LogoutContract
import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionContract
import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionContract.Status.NotAuthorizedFailure
import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionContract.Status.Success
import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionContract.Status.UnableToPrepareThreatProtectionFailure
import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveConnectionSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveProtocolSettingsContract
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.InternetProtocol
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings.OpenVpn
import com.wlvpn.consumervpn.domain.value.settings.StartupConnectOption
import com.wlvpn.consumervpn.util.catchOrEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val retrieveAllConnectionSettingsInteractor:
    RetrieveAllConnectionSettingsContract.Interactor,
    private val saveConnectionSettingsInteractor: SaveConnectionSettingsContract.Interactor,
    private val saveProtocolSettingsInteractor: SaveProtocolSettingsContract.Interactor,
    private val prepareThreatProtectionInteractor: PrepareThreatProtectionContract.Interactor,
    private val logoutInteractor: LogoutContract.Interactor
) : ViewModel() {

    private val event: MutableStateFlow<SettingsEvent> =
        MutableStateFlow(SettingsEvent.LoadingDataEvent)
    val settingsEvent: StateFlow<SettingsEvent> = event.asStateFlow()

    private var protocolSettings: ProtocolSettings? = null
    private var connectionSettings: ConnectionSettings? = null

    init {
        getSettings()
    }

    fun onAutoStartupSelected(startupConnectOption: StartupConnectOption) {
        connectionSettings?.let {
            connectionSettings = it.copy(startupConnectOption = startupConnectOption)
        }

        saveConnectionSettings()
    }

    fun onAllowLanSelected(value: Boolean) {
        protocolSettings?.let {
            protocolSettings = when (it) {
                is ProtocolSettings.IKEv2 ->
                    it.copy(allowLan = value)

                is ProtocolSettings.OpenVpn ->
                    it.copy(allowLan = value)

                is ProtocolSettings.Wireguard ->
                    it.copy(allowLan = value)
            }
        }

        saveProtocolSettings()
    }

    fun onThreatProtectionSelected(value: Boolean) {
        connectionSettings?.let {
            connectionSettings = it.copy(isThreatProtectionEnabled = value)
        }

        if (value) {

            viewModelScope.launch(Dispatchers.IO) {
                prepareThreatProtectionInteractor.execute()
                    .catchOrEmpty {
                        event.value = SettingsEvent.ErrorPreparingThreatProtection
                    }.collect {
                        when (it) {
                            Success -> saveConnectionSettings()
                            NotAuthorizedFailure,
                            is UnableToPrepareThreatProtectionFailure ->
                                event.value = SettingsEvent.ErrorPreparingThreatProtection
                        }
                    }
            }
        } else {
            saveConnectionSettings()
        }
    }

    fun onOverrideMtuSelected(value: Boolean) {
        protocolSettings?.let {
            when (it) {
                is OpenVpn ->
                    protocolSettings = it.copy(overrideMtu = value)

                else -> {
                    // No-op
                }
            }
        }
        saveProtocolSettings()
    }

    fun onVpnProtocolSelected(protocol: Protocol) {
        connectionSettings?.let {
            connectionSettings = it.copy(selectedProtocol = protocol)
        }

        saveConnectionSettings()
    }

    fun onScrambleClick(value: Boolean) {
        protocolSettings?.let {
            when (it) {
                is ProtocolSettings.OpenVpn -> {
                    protocolSettings = it.copy(scramble = value)
                }

                is ProtocolSettings.IKEv2,
                is ProtocolSettings.Wireguard -> {
                }
            }
        }

        saveProtocolSettings()
    }

    fun onProtocolClick(internetProtocol: InternetProtocol) {
        protocolSettings?.let {
            when (it) {
                is ProtocolSettings.OpenVpn -> {
                    protocolSettings = it.copy(internetProtocol = internetProtocol)
                }

                is ProtocolSettings.IKEv2,
                is ProtocolSettings.Wireguard -> {
                }
            }
        }

        saveProtocolSettings()
    }

    fun onPortClick(port: Int) {
        protocolSettings?.let {
            when (it) {
                is ProtocolSettings.OpenVpn -> {
                    protocolSettings = it.copy(
                        port = if (it.scramble) OpenVpnPort.Scramble(port)
                        else OpenVpnPort.Normal(port)
                    )
                }

                is ProtocolSettings.IKEv2,
                is ProtocolSettings.Wireguard -> {
                }
            }
        }

        saveProtocolSettings()
    }

    fun onLogout() {
        viewModelScope.launch(Dispatchers.IO) {

            logoutInteractor.execute().
            catch { throwable ->
                Timber.e(throwable, "Error on executing logout")
                event.value = SettingsEvent.UnableToLogout
            }.collect { status ->
                val eventLogout = when (status) {
                    LogoutContract.Status.ExpiredAccessToken -> SettingsEvent.ExpiredAccessToken
                    LogoutContract.Status.InvalidAccessToken -> SettingsEvent.InvalidAccessToken
                    LogoutContract.Status.Success -> SettingsEvent.SuccessLogout
                    LogoutContract.Status.UnableToLogout -> SettingsEvent.UnableToLogout
                }
                event.value = eventLogout
            }
        }
    }

    private fun getSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            retrieveAllConnectionSettingsInteractor.execute()
                .catch { Timber.e("Error Getting settings") }
                .collect {
                    event.value = when (it) {
                        is RetrieveAllConnectionSettingsContract.Status.Success -> {
                            protocolSettings = it.protocolSettings
                            connectionSettings = it.connectionSettings
                            SettingsEvent.SettingsReceived(
                                connectionSettings = it.connectionSettings,
                                protocolSettings = it.protocolSettings,
                                availableVpnPorts = it.availableVpnPorts
                            )
                        }

                        is RetrieveAllConnectionSettingsContract.Status
                        .UnableToRetrieveSettingsFailure -> SettingsEvent.ErrorGettingSettings
                    }
                }
        }
    }

    private fun saveProtocolSettings() {
        protocolSettings?.let {
            viewModelScope.launch(Dispatchers.IO) {

                saveProtocolSettingsInteractor.execute(it).collect {
                    when (it) {
                        SaveProtocolSettingsContract.Status.Success -> {
                            getSettings()
                        }

                        SaveProtocolSettingsContract.Status.UnableToCompleteFailure -> {
                            Timber.e("Could not save settings")
                        }
                    }
                }
            }
        }
    }

    private fun saveConnectionSettings() {
        Timber.i("Saving: $connectionSettings")
        connectionSettings?.let {
            viewModelScope.launch(Dispatchers.IO) {

                saveConnectionSettingsInteractor.execute(it).collect {
                    when (it) {
                        SaveConnectionSettingsContract.Status.Success -> {
                            getSettings()
                        }

                        SaveConnectionSettingsContract.Status.UnableToSaveFailure -> {
                            Timber.e("Could not save settings")
                        }
                    }
                }
            }
        }
    }
}
