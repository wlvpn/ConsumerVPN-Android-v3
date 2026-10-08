package com.wlvpn.consumervpn.presentation.diagnostics

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val event by viewModel.diagnosticsEvent.observeAsState(DiagnosticsEvent.LoadingInProgress)

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        containerColor = LocalColors.current.scheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalColors.current.scheme.background,
                    titleContentColor = LocalColors.current.scheme.onBackground,
                ),
                title = { Text(stringResource(R.string.settings_screen_label_diagnostics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                    IconButton(onClick = { viewModel.shareDiagnostics() }) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val currentEvent = event) {
                is DiagnosticsEvent.LoadingInProgress -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is DiagnosticsEvent.Error -> {
                    Text(
                        text = stringResource(R.string.diagnostics_screen_not_available),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DiagnosticsEvent.DiagnosticsLoaded -> {
                    if (currentEvent.sharePath != null) {
                        LaunchedEffect(currentEvent.sharePath) {
                            shareDiagnosticsFile(context, currentEvent.sharePath)
                            viewModel.onShareHandled()
                        }
                    }

                    // Delete Confirmation Dialog
                    if (showDeleteDialog) {
                        AlertDialog(
                            text = {
                                Text(
                                    text = stringResource(R.string.diagnostics_screen_clear_dialog_text)
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    viewModel.clearDiagnostics()
                                }) {
                                    Text(
                                        text = stringResource(R.string.diagnostics_screen_clear_dialog_button_delete)
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text(
                                        text = stringResource(R.string.diagnostics_screen_clear_dialog_button_cancel)
                                    )
                                }
                            },
                            onDismissRequest = { showDeleteDialog = false }
                        )
                    }

                    // Container Box wrapping the diagnostics content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        if (currentEvent.diagnostics.isEmpty()) {
                            Text(
                                text = stringResource(R.string.diagnostics_screen_not_available),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            // High performance LazyColumn rendering
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(currentEvent.diagnostics) { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareDiagnosticsFile(context: Context, filePath: String) {
    val file = File(filePath)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(sendIntent, null))
}