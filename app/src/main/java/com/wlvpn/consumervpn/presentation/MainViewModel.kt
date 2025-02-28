package com.wlvpn.consumervpn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.application.interactor.GetUserSessionContract
import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserContract
import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserContract.Status.MigrationSuccess
import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserContract.Status.NoMigrationNeeded
import com.wlvpn.consumervpn.application.interactor.login.MigrateLegacyUserContract.Status.UnableToMigrateFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserSessionInteractor: GetUserSessionContract.Interactor,
    private val migrateLegacyUserInteractor: MigrateLegacyUserContract.Interactor
) : ViewModel() {

    private val mutableMainViewEvent: MutableStateFlow<MainScreenEvent> =
        MutableStateFlow(MainScreenEvent.Loading)
    val mainViewEvent = mutableMainViewEvent.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val migrationStatus = migrateLegacyUserInteractor.execute().firstOrNull()

            when (migrationStatus) {
                MigrationSuccess,
                NoMigrationNeeded -> {
                    // No-op
                }

                null,
                is UnableToMigrateFailure -> {
                    mutableMainViewEvent.emit(MainScreenEvent.MigrationError)
                }
            }

            checkLogin()
        }
    }

    private suspend fun checkLogin() {
        getUserSessionInteractor.execute().collect { status ->
            when (status) {
                is GetUserSessionContract.Status.Success -> {
                    val isLoggedIn = status.userSession.isLoggedIn

                    Timber.d("Is logged in: $isLoggedIn")

                    val event =
                        if (isLoggedIn) MainScreenEvent.UserLoggedIn
                        else MainScreenEvent.UserNotLoggedIn

                    mutableMainViewEvent.emit(event)

                    Timber.d("Main screen event: $event")
                }

                is GetUserSessionContract.Status.UnableToGetUserSessionFailure -> {
                    Timber.e(status.throwable, "Unable to obtain the credentials")
                }
            }
        }
    }
}

sealed class MainScreenEvent {
    data object Loading : MainScreenEvent()
    data object MigrationError : MainScreenEvent()
    data object UserLoggedIn : MainScreenEvent()
    data object UserNotLoggedIn : MainScreenEvent()
}