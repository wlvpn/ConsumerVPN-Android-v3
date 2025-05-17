package com.wlvpn.consumervpn.presentation.locations

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.SearchMatchType
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.domain.value.ServerLocation.City
import com.wlvpn.consumervpn.domain.value.ServerLocation.Country
import com.wlvpn.consumervpn.domain.value.ServerLocation.Fastest
import com.wlvpn.consumervpn.domain.value.ServerLocation.Server
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens
import com.wlvpn.consumervpn.presentation.util.getUriForFlag
import kotlinx.coroutines.launch

@Composable
inline fun LocationsScreen(viewModel: LocationsViewModel, crossinline onConnect: () -> Unit) {

    val context = LocalContext.current

    val rowHeight = dimensionResource(id = R.dimen.spacing_xxxxlarge)
    val rowPadding = dimensionResource(id = R.dimen.spacing_xsmall)

    val activity = LocalContext.current as Activity
    val colorAppBar = LocalColors.current.scheme.background

    var requestConnection by remember { mutableStateOf(false) }
    var currentConnectionTarget by remember { mutableStateOf<ServerLocation>(ServerLocation.Fastest) }

    var citySortState by remember { mutableStateOf(false) }

    SideEffect {
        activity.window?.apply {
            // Set the status bar color
            statusBarColor = colorAppBar.toArgb()

        }
    }

    val locationsEvent = viewModel.locationsEvent.observeAsState()
    val saveLocationEvent by viewModel.saveLocationStateFlow.collectAsState()

    if (saveLocationEvent is LocationsEvent.SelectedLocationSaved){
        onConnect()
    }

    Scaffold(
        topBar = {
            LocationsTopAppBar(onSearchText = {
                viewModel.searchText(it)
            }, sortByCity = {
                viewModel.sortByCity()
                citySortState = true
            }, sortByCountry =  {
                viewModel.sortByCountry()
                citySortState = false
            }, onCancelSearch =  {
                if (citySortState) {
                    viewModel.sortByCity()
                } else {
                    viewModel.sortByCountry()
                }
            })
        }
    ) { paddingValues ->
        when (val event = locationsEvent.value) {
            is LocationsEvent.CityLocationListLoaded ->
                CitiesContent(
                    paddingValues,
                    event.cityLocationList,
                    event.savedTarget,
                    rowHeight,
                    rowPadding,
                    connect = {
                        currentConnectionTarget = it
                        requestConnection = true
                    }
                )

            LocationsEvent.ConnectionRequestFailure -> {}
            LocationsEvent.ConnectionRequestSuccess ->
                LaunchedEffect(Unit) { onConnect() }

            is LocationsEvent.CountryLocationListLoaded ->
                CountriesContent(
                    paddingValues,
                    event.countryLocationList,
                    event.savedTarget,
                    rowHeight,
                    rowPadding,
                    connect = {
                        currentConnectionTarget = it
                        requestConnection = true
                    }
                )

            LocationsEvent.ListLoadingInProgress -> {}
            LocationsEvent.NoNetworkFailure -> {}
            LocationsEvent.SelectedLocationSaved -> {}
            LocationsEvent.ServerRefreshed -> {}
            LocationsEvent.UnableToLoadListFailure -> {}
            LocationsEvent.UnableToSaveSelectedLocation -> {}
            LocationsEvent.UnableToSortListFailure -> {}
            is LocationsEvent.UnknownErrorFailure -> {}
            LocationsEvent.UserNotAuthenticated -> {}
            LocationsEvent.InitialSearchState -> {}
            LocationsEvent.NoSearchResults -> {
                NoSearchResults(stringResource(id = string.locations_screen_search_no_result))
            }
            LocationsEvent.SearchingLocations -> {}
            LocationsEvent.UnableToRetrieveSearchLocations -> {}
            LocationsEvent.UnableToUpdateConnectionTarget -> {}
            null -> {}
        }
    }

    if (requestConnection) {
        ConnectDialog(
            connectionTarget = currentConnectionTarget,
            onDismiss = {
                requestConnection = false
            },
            onConfirm = {
                viewModel.saveServerLocationToConnect(currentConnectionTarget)
                requestConnection = false
            }
        )
    }
}

@Composable
fun CitiesContent(
    paddingValues: PaddingValues,
    cityLocationList: List<ServerLocation.City>,
    currentTarget: ConnectionTarget,
    rowHeight: Dp,
    rowPadding: Dp,
    connect: (location: ServerLocation) -> Unit
) {
    LazyColumn(modifier = Modifier
        .padding(paddingValues)
    ) {
        item {
            FastestAvailable(
                modifier = Modifier.clickable {
                    connect(ServerLocation.Fastest)
                },
                rowHeight,
                rowPadding
            )
        }

        itemsIndexed(cityLocationList) { _, city ->
            val isCitySelected = currentTarget is ConnectionTarget.City &&
                    currentTarget.country.code == city.country.code &&
                    currentTarget.name == city.name

            val color = if (isCitySelected)
                LocalColors.current.extendedColors.controlHighlightColor
            else
                LocalColors.current.extendedColors.controlNormalColor
            Row(modifier = Modifier
                .height(rowHeight)
                .fillMaxWidth()
                .background(
                    if (isCitySelected)
                        LocalColors.current.extendedColors.backgroundLocationHighlight
                    else LocalColors.current.extendedColors.backgroundLocationNormal
                )
                .padding(rowPadding)
                .clickable { connect(city) }
            ) {
                LocationContent(
                    text = "${city.name}, ${city.country.name}",
                    textColor = color,
                    country = city.country,
                    rowPadding
                )
            }
        }
    }
}

@Composable
fun LocationContent(
    text: String,
    textColor: Color,
    country: ServerLocation.Country,
    rowPadding: Dp
) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Flag(country = country)
        Text(text = text, color = textColor,
            modifier = Modifier.padding(
                start = rowPadding
            )
        )
    }

}
@Composable
fun Flag(country: ServerLocation.Country) {
    val context = LocalContext.current

    AsyncImage(model = country.getUriForFlag(context = context),
        contentDescription = null, contentScale = ContentScale.Crop,
        placeholder = BrushPainter(
            Brush.linearGradient(
                listOf(
                    LocalColors.current.extendedColors.controlHighlightColor,
                    LocalColors.current.extendedColors.backgroundItemLocation,
                )
            )
        ),
        modifier = Modifier
            .size(
                dimensionResource(id = R.dimen.locations_flag_width),
                dimensionResource(id = R.dimen.locations_flag_height)
            )
            .clip(CircleShape)
    )
}

@Composable
fun CountriesContent(
    paddingValues: PaddingValues,
    countryLocationList: List<ServerLocation.Country>,
    currentTarget: ConnectionTarget,
    rowHeight: Dp,
    rowPadding: Dp,
    connect: (location: ServerLocation) -> Unit
) {
    val expandedState = remember(countryLocationList) {
        mutableStateOf(
            countryLocationList.map {
                it.searchedBy == SearchMatchType.CityName
            }
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues),
        state = scrollState
    ) {
        item {
            FastestAvailable(
                modifier = Modifier.clickable {
                    connect(ServerLocation.Fastest)
                },
                rowHeight,
                rowPadding
            )
        }
        itemsIndexed(countryLocationList) { index, country ->
            val isSelected = currentTarget is ConnectionTarget.Country &&
                    currentTarget.code == country.code

            val color = if (isSelected)
                LocalColors.current.extendedColors.controlHighlightColor
            else
                LocalColors.current.extendedColors.controlNormalColor
            Row(modifier = Modifier
                .animateItem()
                .height(rowHeight)
                .fillMaxWidth()
                .clickable { connect(country) }
                .background(
                    if (isSelected)
                        LocalColors.current.extendedColors.backgroundLocationHighlight
                    else LocalColors.current.extendedColors.backgroundLocationNormal
                )
                .padding(rowPadding)
            ) {
                LocationContent(
                    text = country.name,
                    textColor = color,
                    country = country,
                    rowPadding = rowPadding
                )

                if (country.cities.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                    ElevatedButton(modifier = Modifier
                        .defaultMinSize(
                            minWidth = dimensionResource(id = R.dimen.spacing_xxxsmall),
                            minHeight = dimensionResource(id = R.dimen.spacing_xxxsmall)
                        ),
                        colors = ButtonDefaults.elevatedButtonColors(
                            contentColor = color,
                            containerColor =
                                LocalColors.current.extendedColors.backgroundButtonLocation
                        ), onClick = {
                            if (index == countryLocationList.size - 1) {
                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(
                                        index = countryLocationList.size
                                    )
                                }
                            }
                            expandedState.value = expandedState.value
                                .toMutableList()
                                .apply {
                                    this[index] = !this[index]
                                }
                        }) {
                        Text(text = pluralStringResource(
                            id = R.plurals.locations_screen_label_cities_number,
                            count = country.cities.size,
                            country.cities.size).uppercase()
                        )
                    }
                }
            }

            if (expandedState.value.getOrNull(index) == true) {
                country.cities.forEach {
                    val isCitySelected = currentTarget is ConnectionTarget.City &&
                            currentTarget.country.code == country.code &&
                            currentTarget.name == it.name

                    val cityColor =
                        if (isCitySelected)
                            LocalColors.current.extendedColors.controlHighlightColor
                        else
                            LocalColors.current.extendedColors.controlNormalColor
                    Row(modifier = Modifier
                        .background(LocalColors.current.extendedColors.backgroundItemLocation)
                        .fillMaxWidth()
                        .height(rowHeight)
                        .clickable { connect(it) }
                        .padding(start = rowPadding)
                        .padding(start = dimensionResource(id = R.dimen.locations_flag_width))
                        .padding(start = rowPadding),
                        verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                color = cityColor,
                                text = it.name
                            )
                    }
                }
            }
        }
    }
}

@Composable
fun FastestAvailable(
    modifier: Modifier,
    height: Dp,
    rowPadding: Dp
) {

    Row(modifier = modifier
        .background(LocalColors.current.extendedColors.backgroundLocationNormal)
        .fillMaxWidth()
        .height(height)
        .padding(rowPadding),
        verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_fastest_server),
            contentDescription = "",
            colorFilter = ColorFilter.tint(LocalColors.current.extendedColors.controlNormalColor)
        )
        Text(modifier = Modifier
            .padding(
                start = rowPadding
            ), text = stringResource(id = string.locations_screen_label_fastest_available),
            color = LocalColors.current.extendedColors.controlNormalColor
        )

    }
}

@Composable
fun NoSearchResults(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}

@Composable
@Preview
fun CitiesContentPreview() {

    Scaffold(
        containerColor = LocalColors.current.extendedColors.backgroundLocationNormal,
        topBar = {
            LocationsTopAppBar(
                onSearchText = {},
                sortByCity = {},
                sortByCountry =  {},
                onCancelSearch = {}
            )
        }
    ) { paddingValues ->
        CitiesContent(paddingValues = paddingValues,
            cityLocationList = listOf(
                ServerLocation.City(ServerLocation.Country(
                    name = "Mexico", code = "mx"
                ), name = "Guadalajara"),
                ServerLocation.City(ServerLocation.Country(
                    name = "Mexico", code = "mx"
                ), name = "Zapopan")
            ),
            currentTarget = ConnectionTarget.City(
                ConnectionTarget.Country("mx"),
                "Zapopan"),
            dimensionResource(id = R.dimen.spacing_xxxxlarge),
            dimensionResource(id = R.dimen.spacing_xsmall)
        ) {}
    }
}

@Composable
@Preview
fun CountriesContentPreview() {

    Scaffold(
        containerColor = LocalColors.current.extendedColors.backgroundLocationNormal,
        topBar = {
            LocationsTopAppBar(
                onSearchText = {},
                sortByCity = {},
                sortByCountry =  {},
                onCancelSearch = {}
            )
        }
    ) { paddingValues ->
        CountriesContent(
            paddingValues = paddingValues,
            countryLocationList = listOf(
                ServerLocation.Country(
                    name = "Mexico", code = "mx",
                    cities = listOf(ServerLocation.City(name = "Guadalajara"))
                ),
                ServerLocation.Country(
                    name = "United States", code = "us"
                )
            ),
            currentTarget = ConnectionTarget.Country(code = "us"),
            dimensionResource(id = R.dimen.spacing_xxxxlarge),
            dimensionResource(id = R.dimen.spacing_xsmall),
        ) {}
    }

}

@Composable
fun ConnectDialog(
    modifier: Modifier = Modifier,
    connectionTarget: ServerLocation,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        shape = RoundedCornerShape(LocalDimens.current.xxSmall),
        containerColor = LocalColors.current.scheme.onPrimaryContainer,
        title = {
            Text(
                color = LocalColors.current.scheme.surface,
                text = stringResource(string.locations_dialog_title_label),
            )
        },
        text = {
            val target = when (connectionTarget) {
                is City -> "${connectionTarget.name}, ${connectionTarget.country.name}"
                is Country -> connectionTarget.name
                Fastest -> stringResource(string.locations_screen_label_fastest_available)
                is Server -> stringResource(string.locations_screen_search_no_result)
            }
            Text(
                color = LocalColors.current.scheme.surface,
                text = stringResource(string.locations_dialog_connect_to_label, target)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) {
                Text(
                    color = LocalColors.current.scheme.primaryContainer,
                    text = stringResource(string.locations_dialog_connect_button_label)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(
                    color = LocalColors.current.scheme.primaryContainer,
                    text = stringResource(string.locations_dialog_cancel_button_label)
                )
            }
        },
        onDismissRequest = {
            onDismiss()
        },
    )
}