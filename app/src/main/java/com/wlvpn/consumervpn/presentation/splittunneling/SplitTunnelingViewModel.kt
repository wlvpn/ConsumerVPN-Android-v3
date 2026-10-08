package com.wlvpn.consumervpn.presentation.splittunneling

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.settings.RetrieveAllConnectionSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.SaveConnectionSettingsContract
import com.wlvpn.consumervpn.application.interactor.settings.splittunneling.RetrieveInstalledAppsContract
import com.wlvpn.consumervpn.domain.value.settings.SplitTunnelSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SplitTunnelingViewModel @Inject constructor(
    private val retrieveInstalledAppsInteractor: RetrieveInstalledAppsContract.Interactor,
    private val retrieveAllConnectionSettingsInteractor:
    RetrieveAllConnectionSettingsContract.Interactor,
    private val saveConnectionSettingsInteractor: SaveConnectionSettingsContract.Interactor
) : ViewModel() {

    val splitTunnelingEvent = MutableLiveData<SplitTunnelingEvent>()

    private val saveMutex = Mutex()

    private var loadAppsJob: Job? = null

    fun loadApps() {
        // Reading the permissions of every installed package is expensive, a load already in
        // flight will deliver a fresh list so there is no reason to start a second one
        if (loadAppsJob?.isActive == true) return

        splitTunnelingEvent.postValue(SplitTunnelingEvent.LoadingInProgress)

        loadAppsJob = viewModelScope.launch(Dispatchers.IO) {
            retrieveInstalledAppsInteractor.execute()
                .catch { throwable ->
                    Timber.e(throwable, "Error while retrieving installed apps")
                    splitTunnelingEvent.postValue(SplitTunnelingEvent.UnableToLoadFailure)
                }
                .collectLatest { status ->
                    when (status) {
                        is RetrieveInstalledAppsContract.Status.Success -> {
                            val connectionSettings =
                                retrieveAllConnectionSettingsInteractor.execute()
                                    .catch { Timber.e(it, "Error getting settings") }
                                    .filterIsInstance<RetrieveAllConnectionSettingsContract
                                        .Status.Success>()
                                    .map { it.connectionSettings }
                                    .firstOrNull()

                            val savedExclusions = when (
                                val stSettings = connectionSettings?.splitTunnelSettings
                            ) {
                                is SplitTunnelSettings.DisallowedApps ->
                                    stSettings.excludedAppPackages.toSet()
                                else -> emptySet()
                            }

                            val filters = listOf(
                                SplitTunnelSettings.Filter.User,
                                SplitTunnelSettings.Filter.System
                            )

                            splitTunnelingEvent.postValue(
                                SplitTunnelingEvent.AppsLoaded(
                                    availableFilters = filters,
                                    selectedFilter = null,
                                    apps = status.apps,
                                    excludedPackages = savedExclusions
                                )
                            )
                        }

                        is RetrieveInstalledAppsContract.Status.UnableToRetrieveAppsFailure -> {
                            Timber.e(status.throwable, "Error while retrieving installed apps")
                            splitTunnelingEvent.postValue(SplitTunnelingEvent.UnableToLoadFailure)
                        }
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val currentState = splitTunnelingEvent.value as? SplitTunnelingEvent.AppsLoaded ?: return
        splitTunnelingEvent.postValue(currentState.copy(searchQuery = query))
    }

    fun toggleSearch(isActive: Boolean) {
        val currentState = splitTunnelingEvent.value as? SplitTunnelingEvent.AppsLoaded ?: return
        splitTunnelingEvent.postValue(
            currentState.copy(
                isSearchActive = isActive,
                searchQuery = if (!isActive) "" else currentState.searchQuery
            )
        )
    }

    fun onFilterSelected(filter: SplitTunnelSettings.Filter) {
        val currentState = splitTunnelingEvent.value as? SplitTunnelingEvent.AppsLoaded ?: return
        val newFilter = if (currentState.selectedFilter == filter) null else filter
        splitTunnelingEvent.postValue(currentState.copy(selectedFilter = newFilter))
    }

    fun onAppSelected(packageName: String, isChecked: Boolean) {
        val currentState = splitTunnelingEvent.value as? SplitTunnelingEvent.AppsLoaded ?: return

        val updatedExclusions = if (isChecked) {
            currentState.excludedPackages + packageName
        } else {
            currentState.excludedPackages - packageName
        }

        splitTunnelingEvent.postValue(
            currentState.copy(excludedPackages = updatedExclusions)
        )

        saveExclusionsToDataStore(updatedExclusions)
    }

    private fun getVisiblePackages(state: SplitTunnelingEvent.AppsLoaded): Set<String> =
        state.visibleApps.mapTo(mutableSetOf()) { it.packageName }

    fun onSelectAll() {
        val currentState = splitTunnelingEvent.value as? SplitTunnelingEvent.AppsLoaded ?: return

        val updatedExclusions = currentState.excludedPackages + getVisiblePackages(currentState)

        splitTunnelingEvent.postValue(currentState.copy(excludedPackages = updatedExclusions))
        saveExclusionsToDataStore(updatedExclusions)
    }

    fun onDeselectAll() {
        val currentState = splitTunnelingEvent.value as? SplitTunnelingEvent.AppsLoaded ?: return

        val updatedExclusions = currentState.excludedPackages - getVisiblePackages(currentState)

        splitTunnelingEvent.postValue(currentState.copy(excludedPackages = updatedExclusions))
        saveExclusionsToDataStore(updatedExclusions)
    }

    private fun saveExclusionsToDataStore(exclusions: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                val currentSettings = retrieveAllConnectionSettingsInteractor.execute()
                    .catch { Timber.e(it, "Error getting settings") }
                    .filterIsInstance<RetrieveAllConnectionSettingsContract.Status.Success>()
                    .map { it.connectionSettings }
                    .firstOrNull() ?: return@withLock

                val updatedSettings = currentSettings.copy(
                    splitTunnelSettings = SplitTunnelSettings.DisallowedApps(exclusions.toList())
                )

                saveConnectionSettingsInteractor.execute(updatedSettings)
                    .catch { throwable ->
                        Timber.e(throwable, "Error saving settings")
                    }
                    .collectLatest { status ->
                        when (status) {
                            is SaveConnectionSettingsContract.Status.Success -> {
                                // No op.
                            }

                            is SaveConnectionSettingsContract.Status.UnableToSaveFailure -> {
                                Timber.e("Failed to save Split Tunnel exclusions.")
                            }
                        }
                    }
            }
        }
    }
}