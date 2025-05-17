package com.wlvpn.consumervpn.presentation.locations

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsTopAppBar(
    sortByCountry: () -> Unit,
    sortByCity: () -> Unit,
    onSearchText: (text: String) -> Unit,
    onCancelSearch: () -> Unit
) {
    var expandedSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var countrySelected by remember {
        mutableStateOf(true)
    }
    var citySelected by remember {
        mutableStateOf(false)
    }
    var showMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = LocalColors.current.scheme.background,
            titleContentColor = LocalColors.current.extendedColors.controlNormalColor,
            actionIconContentColor = LocalColors.current.extendedColors.controlNormalColor
        ),

        title = {
            if (!expandedSearch) {
                Text(stringResource(id = R.string.locations_screen_label_title))
            }
        },
        actions = {
            if (expandedSearch) {
                OutlinedTextField(
                    modifier = Modifier.focusRequester(focusRequester)
                        .onGloballyPositioned { focusRequester.requestFocus() },
                    value = searchText,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                        LocalColors.current.extendedColors.controlNormalColor,
                        unfocusedBorderColor =
                        LocalColors.current.extendedColors.controlNormalColor,
                        unfocusedLeadingIconColor =
                        LocalColors.current.extendedColors.controlNormalColor,
                        focusedLeadingIconColor =
                        LocalColors.current.extendedColors.controlNormalColor
                    ),
                    onValueChange = {
                        searchText = it
                        onSearchText(it)
                    }, leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) })
                IconButton(onClick = {
                    expandedSearch = false
                    searchText = ""
                    onCancelSearch()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Localized description"
                    )
                }
            } else {
                IconButton(
                    onClick = { expandedSearch = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = LocalColors.current.extendedColors.controlNormalColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Localized description"
                    )
                }
            }

            IconButton(
                onClick = { showMenu = !showMenu },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = LocalColors.current.extendedColors.controlNormalColor
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Sort,
                    contentDescription = "Localized description"
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(
                    LocalColors.current.scheme.background
                )
            ) {
                MenuItemWithCheckMark(stringResource(
                    id =
                    R.string.locations_screen_sort_menu_country
                ), countrySelected, onClick = {
                    showMenu = false
                    countrySelected = true
                    citySelected = false
                    expandedSearch = false
                    searchText = ""
                    sortByCountry()
                }
                )
                MenuItemWithCheckMark(stringResource(id = R.string.locations_screen_sort_menu_city),
                    citySelected, onClick = {
                        showMenu = false
                        citySelected = true
                        countrySelected = false
                        expandedSearch = false
                        searchText = ""
                        sortByCity()
                    }
                )
            }
        }
    )
}

@Composable
fun MenuItemWithCheckMark(text: String, showIcon: Boolean, onClick: () -> Unit) {
    if (showIcon) {
        DropdownMenuItem(text = { Text(text) }, trailingIcon = {
            Icon(
                Icons.Filled.Check, contentDescription = null
            )
        },
            onClick = onClick
        )
    } else {
        DropdownMenuItem(text = { Text(text) }, onClick = onClick)
    }
}