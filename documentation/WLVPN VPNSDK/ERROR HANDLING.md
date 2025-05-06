# Error handling

The **VpnSdk2** has two ways of handling errors: Failure responses and exceptions.

### Failures responses

All SDK's functions return a _Response_ object (see
[Usage: Response classes](./USAGE.md#response-sealed-classes)), which is a Sealed Class with
N number of "_Status_" that represent a use case success and failure paths.

```kotlin
// LoginResponse definition in VpnAccount interface
sealed class LoginResponse {
    // Success path
    object Success : LoginResponse()

    // Failure paths
    object EmptyPassword : LoginResponse()
    object EmptyUsername : LoginResponse()
    object NotConnected : LoginResponse()
    object InvalidCredentials : LoginResponse()
    object InvalidVpnSdkApiConfig : LoginResponse()
    object TooManyAttempts : LoginResponse()
    data class UnableToLogin(val throwable: Throwable) : LoginResponse()
    data class ServiceError(val code: Int, val reason: String?) : LoginResponse()
}
```

Their purpose is self explained in their name:

```kotlin
when (loginResponse) {
    Success -> {
        /** Successful login **/
    }
    EmptyPassword -> {
        /** UserCredential password string is empty **/
    }
    EmptyUsername -> {
        /** UserCredential username string is empty **/
    }
    NotConnected -> {
        /** Not connected to a network **/
    }
    InvalidCredentials -> {
        /** UserCredentials are not valid  **/
    }
    InvalidVpnSdkApiConfig -> {
        /** An SDK configuration is wrongly set (api url, token etc) **/
    }
    TooManyAttempts -> {
        /** The API request was called too quickly in a short time **/
    }
    is UnableToLogin -> {
        /** An unexpected error happend, see the attached throwable **/
    }
    ServiceError -> {
        /** The API backend rejected the request, see the attached code and reason **/
    }
}
```

Some Failures have attributes with more information about why it failed.

### Exceptions

The SDK will avoid to throw an exception in favor of give you a failure instead, if an exceptions
happens the SDK will try to wrap it into a failure named "_UnableToX_" with the throwable as an
argument (eg UnableToConnect or UnableToLogin).

Although the SDK wraps the exceptions into failures, it is still recommended to
use the Flow's `.catch{...}` operator to avoid app crashing if any gets through.

### Error handling sample

```kotlin

class MyViewModel(
    vpnConnection: VpnConnection
) : ViewModel(){

    init {
        viewModelScope.launch(Dispatchers.IO){
            vpnConnection.fetchGeoLocation()
                .catch{
                    // Catch any exception
                }.map{
                    when(it){
                        is Success -> {
                            val geoInfo = it.geoInfo
                            // Update UI
                        }

                        NotConnected -> {
                            // Upadte UI with not connected state
                        }

                        is UnableToFetchGeoLocation -> {
                            val throwable = it.throwable 
                            // Evaluate the throwable if needed and update UI with error state
                        }
                        
                        is ServiceError -> {
                            val message = it.message
                            // Update UI with and show a toast/snakbar/alert with the message
                        }
                    }
                }
        }
    }
}
```
## Failure reference

Check the JavaDoc of each of the sealed classes for more detailed information of their failures.