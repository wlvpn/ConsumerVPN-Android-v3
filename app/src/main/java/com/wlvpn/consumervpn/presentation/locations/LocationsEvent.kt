package com.wlvpn.consumervpn.presentation.locations

import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation

sealed class LocationsEvent {

    object ListLoadingInProgress : LocationsEvent()

    class CountryLocationListLoaded(
        val countryLocationList: List<ServerLocation.Country>,
        val savedTarget: ConnectionTarget
    ) : LocationsEvent()

    class CityLocationListLoaded(
        val cityLocationList: List<ServerLocation.City>,
        val savedTarget: ConnectionTarget
    ) : LocationsEvent()

    object SelectedLocationSaved : LocationsEvent()

    object UnableToLoadListFailure : LocationsEvent()

    object UnableToSortListFailure : LocationsEvent()

    object UnableToSaveSelectedLocation : LocationsEvent()

    object ConnectionRequestSuccess : LocationsEvent()

    object ConnectionRequestFailure : LocationsEvent()

    object NoNetworkFailure : LocationsEvent()

    object ServerRefreshed : LocationsEvent()

    object UserNotAuthenticated : LocationsEvent()

    class UnknownErrorFailure(val throwable: Throwable) : LocationsEvent()

    object InitialSearchState : LocationsEvent()

    object NoSearchResults : LocationsEvent()

    object SearchingLocations : LocationsEvent()

    object UnableToRetrieveSearchLocations : LocationsEvent()
    object UnableToUpdateConnectionTarget : LocationsEvent()
}