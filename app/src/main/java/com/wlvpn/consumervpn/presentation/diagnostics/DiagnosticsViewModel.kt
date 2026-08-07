package com.wlvpn.consumervpn.presentation.diagnostics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.settings.diagnostics.ClearDiagnosticsContract
import com.wlvpn.consumervpn.application.interactor.settings.diagnostics.RetrieveDiagnosticsPathContract
import com.wlvpn.consumervpn.application.interactor.settings.diagnostics.RetrieveDiagnosticsContract
import com.wlvpn.consumervpn.domain.value.DeviceInformation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val retrieveDiagnosticsInteractor: RetrieveDiagnosticsContract.Interactor,
    private val retrieveDiagnosticsPathInteractor: RetrieveDiagnosticsPathContract.Interactor,
    private val clearDiagnosticsInteractor: ClearDiagnosticsContract.Interactor
) : ViewModel() {

    val diagnosticsEvent = MutableLiveData<DiagnosticsEvent>()

    init {
        loadDiagnostics()
    }

    private fun loadDiagnostics() {
        diagnosticsEvent.postValue(DiagnosticsEvent.LoadingInProgress)

        viewModelScope.launch(Dispatchers.IO) {
            retrieveDiagnosticsInteractor.execute()
                .catch { throwable ->
                    Timber.e(throwable, "Error while retrieving diagnostics")
                    diagnosticsEvent.postValue(DiagnosticsEvent.Error)
                }
                .collectLatest { status ->
                    when (status) {
                        is RetrieveDiagnosticsContract.Status.Success -> {
                            diagnosticsEvent.postValue(
                                DiagnosticsEvent.DiagnosticsLoaded(
                                    diagnostics = status.deviceInformation.toLines() +
                                            status.diagnostics
                                )
                            )
                        }
                        is RetrieveDiagnosticsContract.Status.UnableToRetrieveDiagnosticsFailure ->
                        {
                            Timber.e(status.throwable, "Failed to load diagnostics file")
                            diagnosticsEvent.postValue(DiagnosticsEvent.Error)
                        }
                    }
                }
        }
    }

    fun shareDiagnostics() {
        viewModelScope.launch(Dispatchers.IO) {
            retrieveDiagnosticsPathInteractor.execute()
                .catch { throwable ->
                    Timber.e(throwable, "Error getting diagnostics path")
                    diagnosticsEvent.postValue(DiagnosticsEvent.Error)
                }
                .collectLatest { status ->
                    when (status) {
                        is RetrieveDiagnosticsPathContract.Status.Success -> {
                            val currentState = diagnosticsEvent.value
                                    as? DiagnosticsEvent.DiagnosticsLoaded ?: return@collectLatest
                            diagnosticsEvent.postValue(currentState.copy(sharePath = status.path))
                        }
                        is RetrieveDiagnosticsPathContract.Status.UnableToRetrievePathFailure -> {
                            Timber.e(status.throwable, "Failed to retrieve diagnostics path")
                            diagnosticsEvent.postValue(DiagnosticsEvent.Error)
                        }
                    }
                }
        }
    }

    fun clearDiagnostics() {
        diagnosticsEvent.postValue(DiagnosticsEvent.LoadingInProgress)

        viewModelScope.launch(Dispatchers.IO) {
            clearDiagnosticsInteractor.execute()
                .catch { throwable ->
                    Timber.e(throwable, "Error clearing diagnostics")
                    diagnosticsEvent.postValue(DiagnosticsEvent.Error)
                }
                .collectLatest { status ->
                    when (status) {
                        is ClearDiagnosticsContract.Status.Success -> {
                            loadDiagnostics()
                        }
                        is ClearDiagnosticsContract.Status.UnableToClearFailure -> {
                            Timber.e(status.throwable, "Failed to clear diagnostics")
                            diagnosticsEvent.postValue(DiagnosticsEvent.Error)
                        }
                    }
                }
        }
    }

    fun onShareHandled() {
        val currentState = diagnosticsEvent.value as? DiagnosticsEvent.DiagnosticsLoaded ?: return
        diagnosticsEvent.postValue(currentState.copy(sharePath = null))
    }

    private fun DeviceInformation.toLines(): List<String> = listOf(
        "Device:$device ",
        "Brand:$brand ",
        "OS version:$osVersion ",
        "VPN SDK version:$vpnSdkVersion ",
        "App version:$appVersion"
    )
}