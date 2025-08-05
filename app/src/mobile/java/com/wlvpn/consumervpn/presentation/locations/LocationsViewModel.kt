package com.wlvpn.consumervpn.presentation.locations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationContract.Status.NoNetworkFailure
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationContract.Status.Success
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCityLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.RetrieveCountryLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.SearchCityLocationsContract
import com.wlvpn.consumervpn.application.interactor.location.SearchCountryLocationsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract.Status
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
    private val searchCountryLocationsInteractor: SearchCountryLocationsContract.Interactor,
    private val searchCityLocationsInteractor: SearchCityLocationsContract.Interactor,
    private val saveServerLocationToConnectInteractor: SaveServerLocationToConnectContract.Interactor
    ) : ViewModel() {

    val locationsEvent = MutableLiveData<LocationsEvent>()
    val locationsCityEvent = MutableLiveData<LocationsEvent>()
    private var citySortState: LocationsSortingType = LocationsSortingType.ByCountry
    private val saveLocationMutableStateFlow: MutableStateFlow<LocationsEvent?> =
        MutableStateFlow(null)
    val saveLocationStateFlow: StateFlow<LocationsEvent?> = saveLocationMutableStateFlow


    init {
        loadCountryServerLocations()
    }

    fun loadCityServerLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            retrieveCityLocationsInteractor.execute().collectLatest {
                when (it) {
                    is RetrieveCityLocationsContract.Status.Success -> {
                        locationsEvent.postValue(
                            LocationsEvent.CityLocationListLoaded(
                                it.cityLocationsList, savedTarget = it.savedTarget
                            )
                        )
                    }
                    RetrieveCityLocationsContract.Status.UnableToRetrieveCityLocationsFailure -> {}
                }
            }
        }
    }

    private fun loadCountryServerLocations() {

        locationsEvent.postValue(LocationsEvent.ListLoadingInProgress)

        viewModelScope.launch(Dispatchers.IO) {
            retrieveCountryLocationsInteractor
                .execute()
                .catch {
                    Timber.e(it, "Error while loading country locations lists")
                    locationsEvent.postValue(LocationsEvent.UnknownErrorFailure(it))
                }
                .collectLatest { status ->
                    when (status) {
                        is RetrieveCountryLocationsContract.Status.Success -> {
                            locationsEvent.postValue(
                                LocationsEvent.CountryLocationListLoaded(
                                    status.countryLocationsList,
                                    savedTarget = status.savedTarget
                                    )
                            )
                        }

                        RetrieveCountryLocationsContract.Status.
                        UnableToRetrieveCountryLocationsFailure,
                        RetrieveCountryLocationsContract.Status.
                        UnableToRetrieveSelectedServerFailure ->
                            locationsEvent.postValue(LocationsEvent.UnableToLoadListFailure)
                    }
                }
        }
    }

    fun searchText(search: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (citySortState) {
                LocationsSortingType.ByCity -> searchCityLocationsInteractor
                    .execute(search).collectLatest { status ->
                        when (status) {
                            SearchCityLocationsContract.Status.EmptySearchTerm ->
                                loadCityServerLocations()
                            SearchCityLocationsContract.Status.NoSearchResults ->
                                locationsEvent.postValue(LocationsEvent.NoSearchResults)
                            is SearchCityLocationsContract.Status.SearchResults ->
                                locationsEvent.postValue(LocationsEvent.CityLocationListLoaded(
                                 status.locationList, status.savedTarget)
                                )
                            SearchCityLocationsContract.Status.UnableToRetrieveLocationsFailure ->
                                {}
                        }
                }

                LocationsSortingType.ByCountry -> searchCountryLocationsInteractor
                    .execute(search)
                    .collectLatest { status -> when (status) {
                        SearchCountryLocationsContract.Status.EmptySearchTerm ->
                            loadCountryServerLocations()
                        SearchCountryLocationsContract.Status.NoSearchResults ->
                            locationsEvent.postValue(LocationsEvent.NoSearchResults)
                        is SearchCountryLocationsContract.Status.SearchResults ->
                            locationsEvent.postValue(LocationsEvent.CountryLocationListLoaded(
                            status.locationList, status.savedTarget)
                            )
                        SearchCountryLocationsContract.Status.UnableToRetrieveLocationsFailure -> {}
                    }
                }
            }
        }
    }

    fun sortByCountry() {
        citySortState = LocationsSortingType.ByCountry
        loadCountryServerLocations()
    }

    fun sortByCity() {
        citySortState = LocationsSortingType.ByCity
        loadCityServerLocations()
    }

    fun saveServerLocationToConnect(serverLocation: ServerLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            saveServerLocationToConnectInteractor.execute(serverLocation)
                .catch {
                    Timber.e(it, "Error while saving location to connect")
                }.collectLatest {
                    when (it) {
                        Status.Success ->
                            saveLocationMutableStateFlow.value =
                                LocationsEvent.SelectedLocationSaved

                        is UnableToSaveServerLocation -> saveLocationMutableStateFlow.value =
                            LocationsEvent.UnableToSaveSelectedLocation
                    }
                }
        }
    }

}