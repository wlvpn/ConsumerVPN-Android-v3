package com.wlvpn.consumervpn.presentation.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wlvpn.consumervpn.presentation.login.validation.LoginEvent
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.wlvpn.consumervpn.application.interactor.login.LoginContract
import com.wlvpn.consumervpn.domain.value.UserCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import timber.log.Timber

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginInteractor: LoginContract.Interactor
    ) : ViewModel() {

    val loginEvent = MutableLiveData<LoginEvent>()

    fun login(username: String, password: String) {
        loginEvent.postValue(LoginEvent.ExecutingLogin)

        val userCredentials = UserCredentials(
            username,
            password
        )

        viewModelScope.launch(Dispatchers.IO) {

            loginInteractor.execute(userCredentials).
            catch { throwable ->
                Timber.e(throwable, "Error on executing login")
                loginEvent.postValue(LoginEvent.Error(throwable.message ?: ""))
            }.collect { status ->
                val event = when (status) {
                    LoginContract.Status.Success -> LoginEvent.Success

                    LoginContract.Status.EmptyUsernameFailure ->
                        LoginEvent.EmptyUsername
                    LoginContract.Status.EmptyPasswordFailure ->
                        LoginEvent.EmptyPassword

                    LoginContract.Status.InvalidCredentialsFailure -> {
                        LoginEvent.InvalidCredentials
                    }

                    LoginContract.Status.ConnectionFailure ->
                        LoginEvent.NoNetwork
                    LoginContract.Status.NotAuthorizedFailure,

                    LoginContract.Status.TooManyRequestsFailure ->
                        LoginEvent.TooManyAttempts
                    is LoginContract.Status.UnableToLoginFailure ->
                        LoginEvent.UnableToLogin(
                            message = status.message,
                            errorCode = status.errorCode
                        )
                }

                loginEvent.postValue(event)
            }
        }
    }
}
