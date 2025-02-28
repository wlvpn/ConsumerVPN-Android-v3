package com.wlvpn.consumervpn.presentation.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCityLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCountryLocationsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract.Status.Success
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract.Status.UnableToSaveServerLocation
import com.wlvpn.consumervpn.domain.value.ServerLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val retrieveCountryLocationsInteractor: RetrieveCountryLocationsContract.Interactor,
    private val retrieveCityLocationsInteractor: RetrieveCityLocationsContract.Interactor,
    private val saveServerLocationToConnectInteractor:
    SaveServerLocationToConnectContract.Interactor
    ) : ViewModel() {

    private val locationCityMutableStateFlow: MutableStateFlow<LocationsEvent?> =
        MutableStateFlow(null)
    val locationCityStateFlow: StateFlow<LocationsEvent?> = locationCityMutableStateFlow

    private val locationCountryMutableStateFlow: MutableStateFlow<LocationsEvent?> =
        MutableStateFlow(null)
    val locationCountryStateFlow: StateFlow<LocationsEvent?> = locationCountryMutableStateFlow

    private val saveLocationMutableStateFlow: MutableStateFlow<LocationsEvent?> =
        MutableStateFlow(null)
    val saveLocationStateFlow: StateFlow<LocationsEvent?> = saveLocationMutableStateFlow

    init {
        loadCityServerLocations()
        loadCountryServerLocations()
    }

    private fun loadCityServerLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            retrieveCityLocationsInteractor.execute().collectLatest {
                when (it) {
                    is RetrieveCityLocationsContract.Status.Success -> {
                        val list = it.cityLocationsList
                            .sortedWith { cityLocation1, cityLocation2 ->
                                cityLocation1.name.compareTo(cityLocation2.name)
                            }
                        locationCityMutableStateFlow.value = LocationsEvent.CityLocationListLoaded(
                            list, savedTarget = it.savedTarget
                        )
                    }
                    RetrieveCityLocationsContract.Status.UnableToRetrieveCityLocationsFailure -> {}
                }
            }
        }
    }

    private fun loadCountryServerLocations() {

        locationCountryMutableStateFlow.value = LocationsEvent.ListLoadingInProgress

        viewModelScope.launch(Dispatchers.IO) {
            retrieveCountryLocationsInteractor
                .execute()
                .catch {
                    Timber.e(it, "Error while loading country locations lists")
                    locationCountryMutableStateFlow.value = LocationsEvent.UnknownErrorFailure(it)
                }
                .collectLatest { status ->
                    when (status) {
                        is RetrieveCountryLocationsContract.Status.Success -> {
                            locationCountryMutableStateFlow.value = LocationsEvent.CountryLocationListLoaded(
                                status.countryLocationsList,
                                savedTarget = status.savedTarget
                            )
                        }

                        RetrieveCountryLocationsContract.Status.
                        UnableToRetrieveCountryLocationsFailure,
                        RetrieveCountryLocationsContract.Status.
                        UnableToRetrieveSelectedServerFailure ->
                            locationCountryMutableStateFlow.value = LocationsEvent.UnableToLoadListFailure
                    }
                }
        }
    }

    fun saveServerLocationToConnect(serverLocation: ServerLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            saveServerLocationToConnectInteractor.execute(serverLocation)
                .catch {
                    Timber.e(it, "Error while saving location to connect")
                }.collectLatest {
                    when (it) {
                        Success ->
                            saveLocationMutableStateFlow.value =
                                LocationsEvent.SelectedLocationSaved

                        is UnableToSaveServerLocation -> saveLocationMutableStateFlow.value =
                            LocationsEvent.UnableToSaveSelectedLocation
                    }
                }
        }
    }

}