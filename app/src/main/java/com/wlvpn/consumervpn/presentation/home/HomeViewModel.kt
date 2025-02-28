package com.wlvpn.consumervpn.presentation.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToVpnContract
import com.wlvpn.consumervpn.application.interactor.connectivity.DisconnectFromVpnContract
import com.wlvpn.consumervpn.application.interactor.connectivity.FetchGeoLocationContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.Connected
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.Connecting
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.Disconnected
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.VpnError
import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsContract
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FETCH_GEO_INFO_DELAY = 1000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectToVpnInteractor: ConnectToVpnContract.Interactor,
    private val disconnectFromVpnInteractor: DisconnectFromVpnContract.Interactor,
    private val listenToVpnInteractor: ListenVpnStateContract.Interactor,
    private val fetchGeoLocationInteractor: FetchGeoLocationContract.Interactor,
    private val retrieveAllConnectionSettingsInteractor:
    RetrieveAllConnectionSettingsContract.Interactor,
    private val connectToLocationInteractor: ConnectToLocationContract.Interactor
) : ViewModel() {

    val homeEvent = MutableLiveData<HomeEvent>(HomeEvent.Disconnected)
    val failureEvent = MutableLiveData<FailureEvent>()
    val geoLocationEvent = MutableLiveData<GeoLocationEvent>()
    val selectedTargetEvent = MutableLiveData<SelectedTargetEvent>()

    init {
        loadState()
    }

    fun loadState() {
        viewModelScope.launch(Dispatchers.IO) {
            listenToVpnInteractor.execute()
                .collectLatest {
                    when (it) {
                        is Connected -> HomeEvent.Connected(it.server)
                        Connecting -> HomeEvent.Connecting
                        Disconnected -> {
                            loadSelectedTarget()
                            HomeEvent.Disconnected
                        }

                        VpnError -> {
                            loadSelectedTarget()
                            HomeEvent.DisconnectedError
                        }
                    }.run {
                        homeEvent.postValue(this)
                        if (this is HomeEvent.Connected) {
                            loadGeoLocation()
                        }
                    }
                }
        }
    }

    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            connectToVpnInteractor.execute()
                .collectLatest {
                    if (it is ConnectToVpnContract.Status.NoNetworkFailure) {
                        failureEvent.postValue(FailureEvent.NoNetworkError)
                    }
                }
        }
    }

    fun connect(serverLocation: ServerLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            connectToLocationInteractor.execute(serverLocation)
                .collectLatest {
                    if (it is ConnectToLocationContract.Status.NoNetworkFailure) {
                        failureEvent.postValue(FailureEvent.NoNetworkError)
                    }
                }
        }
    }

    fun disconnect() {
        geoLocationEvent.value = null
        viewModelScope.launch(Dispatchers.IO) {
            disconnectFromVpnInteractor.execute()
                .collectLatest {
                    println("disconnectFromVpnInteractor status $it")
                }
        }
    }

    private suspend fun loadSelectedTarget() {
        val target = retrieveAllConnectionSettingsInteractor.execute()
            .filter { status ->
                status is RetrieveAllConnectionSettingsContract.Status.Success
            }.map { status ->
                (status as RetrieveAllConnectionSettingsContract.Status.Success)
            }.map { settings ->
                settings.connectionSettings.selectedTarget
            }.firstOrNull() ?: ConnectionTarget.Fastest

        selectedTargetEvent.postValue(SelectedTargetEvent.SelectedTargetUpdated(target))
    }

    private fun loadGeoLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(FETCH_GEO_INFO_DELAY)
            fetchGeoLocationInteractor.execute()
                .catch {
                    geoLocationEvent.postValue(GeoLocationEvent.Error)
                }
                .collectLatest { status ->
                    when (status) {
                        is FetchGeoLocationContract.Status.Success ->
                            geoLocationEvent.postValue(
                                GeoLocationEvent.GeoLocationChanged(status.geolocation)
                            )

                        is FetchGeoLocationContract.Status.UnableToFetchGeoLocationFailure ->
                            geoLocationEvent.postValue(GeoLocationEvent.Error)
                    }
                }
        }
    }
}