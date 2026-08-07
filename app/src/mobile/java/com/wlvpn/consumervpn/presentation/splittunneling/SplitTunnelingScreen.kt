package com.wlvpn.consumervpn.presentation.splittunneling

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.string
import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitTunnelingScreen(
    viewModel: SplitTunnelingViewModel,
    onNavigateBack: () -> Unit
) {
    val event by viewModel.splitTunnelingEvent.observeAsState(SplitTunnelingEvent.LoadingInProgress)

    when (val state = event) {
        is SplitTunnelingEvent.LoadingInProgress -> {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LocalColors.current.scheme.background,
                            titleContentColor = LocalColors.current.scheme.onBackground,
                        ),
                        title = {
                            Text(stringResource(id = R.string.settings_screen_label_split_tunneling_title))
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        is SplitTunnelingEvent.UnableToLoadFailure -> {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LocalColors.current.scheme.background,
                            titleContentColor = LocalColors.current.scheme.onBackground,
                        ),
                        title = {
                            Text(stringResource(id = R.string.settings_screen_label_split_tunneling_title))
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(string.split_tunneling_screen_error_unable_to_load),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is SplitTunnelingEvent.AppsLoaded -> {
            var showMenu by remember { mutableStateOf(false) }

            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            // To preserve cursor position across rotations
            var searchTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(
                    TextFieldValue(
                        text = state.searchQuery,
                        selection = TextRange(state.searchQuery.length)
                    )
                )
            }

            // Sync if the ViewModel clears the text
            LaunchedEffect(state.searchQuery) {
                if (state.searchQuery != searchTextFieldValue.text) {
                    searchTextFieldValue = searchTextFieldValue.copy(
                        text = state.searchQuery,
                        selection = TextRange(state.searchQuery.length)
                    )
                }
            }

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                topBar = {
                    if (state.isSearchActive) {

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = LocalColors.current.scheme.background,
                                titleContentColor = LocalColors.current.scheme.onBackground,
                            ),
                            title = {
                                TextField(
                                    value = searchTextFieldValue,
                                    onValueChange = { newValue ->
                                        // Update local state with cursor info, and push text to ViewModel
                                        searchTextFieldValue = newValue
                                        viewModel.onSearchQueryChanged(newValue.text)
                                    },
                                    placeholder = { Text(stringResource(string.split_tunneling_screen_search_placeholder)) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    singleLine = true
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    focusManager.clearFocus()
                                    viewModel.toggleSearch(false)
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        viewModel.onSearchQueryChanged("")
                                    } else {
                                        focusManager.clearFocus()
                                        viewModel.toggleSearch(false)
                                    }
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = null)
                                }
                            }
                        )
                    } else {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = LocalColors.current.scheme.background,
                                titleContentColor = LocalColors.current.scheme.onBackground,
                            ),
                            title = {
                                Text(stringResource(id = R.string.settings_screen_label_split_tunneling_title))
                            },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.toggleSearch(true) }) {
                                    Icon(Icons.Filled.Search, contentDescription = null)
                                }
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(string.split_tunneling_screen_menu_select_all)) },
                                        onClick = {
                                            viewModel.onSelectAll()
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(string.split_tunneling_screen_menu_deselect_all)) },
                                        onClick = {
                                            viewModel.onDeselectAll()
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(state.availableFilters) { filter ->
                            val filterLabel = when (filter) {
                                SplitTunnelSettings.Filter.System -> stringResource(string.split_tunneling_screen_system_apps_label)
                                SplitTunnelSettings.Filter.User -> stringResource(string.split_tunneling_screen_user_apps_label)
                            }

                            FilterChip(
                                selected = state.selectedFilter == filter,
                                onClick = { viewModel.onFilterSelected(filter) },
                                label = { Text(filterLabel) },
                                leadingIcon = if (state.selectedFilter == filter) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    val displayApps = state.apps.filter {
                        val matchesSearch = it.name.contains(state.searchQuery, ignoreCase = true)

                        val matchesFilter = when (state.selectedFilter) {
                            null -> true
                            SplitTunnelSettings.Filter.System -> it.isSystemApp
                            SplitTunnelSettings.Filter.User -> !it.isSystemApp
                        }

                        matchesSearch && matchesFilter
                    }

                    if (displayApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(string.split_tunneling_screen_search_no_results),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(displayApps, key = { it.packageName }) { app ->
                                AppListItem(
                                    app = app,
                                    isChecked = state.excludedPackages.contains(app.packageName),
                                    onCheckedChange = { isChecked ->
                                        viewModel.onAppSelected(app.packageName, isChecked)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    app: SplitTunnelSettings.App,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            appName = app.name,
            packageName = app.packageName,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AppIcon(
    appName: String,
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Load icon asynchronously
    val appDrawable by produceState<Drawable?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    if (appDrawable != null) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                ImageView(ctx).apply {
                    scaleType = ImageView.ScaleType.FIT_XY
                }
            },
            update = { imageView ->
                imageView.setImageDrawable(appDrawable)
            }
        )
    } else {
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}