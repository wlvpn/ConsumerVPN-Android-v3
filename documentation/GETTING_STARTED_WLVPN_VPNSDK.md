# VPN SDK v2

VPN-SDK v2 is a library with all of the necessary components to manage, create and monitor a
VPN connection on Android.

## Setup

1. [Requirements](#requirements)
    1. [Environment](#environment)
    2. [Android Manifest Permissions](#android-manifest-permissions)
2. [Project setup](#project-setup)
    1. [PackageCloud token in gradle.properties](#packagecloud-token-in-gradleproperties)
    2. [PackageCloud repository](#packagecloud-repository)
    3. [Import the SDK](#import-the-sdk)
3. [Library configuration](#library-configuration)
    1. [Creating the VpnSdk instance](#creating-the-vpnsdk-instance)
    2. [Customizing parameters](#customizing-parameters)
    3. [Accessing the instance](#accessing-the-instance)
        1. [Application class](#application-class)
        2. [DI (recommended)](#di--recommended-)
    4. [onVpnRestartTrigger listener](#onvpnrestarttrigger-listener)

### Requirements

#### Environment

- Java 11
- Min Android SDK Level 22
- Gradle version 7.2
- WLVPN API Access token
- PackageCloud Access token

#### Android Manifest Permissions

The following permissions are required your application manifest:

* `android.permission.ACCESS_NETWORK_STATE`: Allows the SDK to get access
  to information about networks.
* `android.permission.INTERNET`: Allows the SDK to execute API calls
  over the Internet.
* `android.permission.ACCESS_WIFI_STATE`: Allows the SDK to get access
  to information about Wi-Fi networks.
* `android.permission.FOREGROUND_SERVICE`: Allows the SDK to run the
  VPN service in the foreground.

### Project setup

#### PackageCloud token in gradle.properties

Add the PackageCloud token un your project/global `gradle.properties` file:


<details open>
  <summary>Gradle DSL</summary>

  ```properties
    # ../gradle.properties
packagecloud_token=<TOKEN>
  ```

</details>

#### PackageCloud repository

In root the `buil.gradle` of your project add the PackageCloud repository:

<details open>
  <summary>Gradle DSL</summary>

  ```groovy
 // .../buil.gradle

buildscript {

    repositories {
        // ...
        maven {
            url "https://packagecloud.io/priv/${packagecloud_token}/cloak/android-vpn-sdk/maven2"
        }
    }
}
  ```

</details>

#### Import the SDK

<details open>
  <summary>Gradle DSL</summary>

  ```groovy
  // ../app/build.gradle

dependencies {
    //...
    implementation "com.wlvpn.vpnsdk:sdkv2:<LATEST-VERSION>"

}
  ```

</details>

### Library configuration

#### Creating the VpnSdk instance

With the VpnSdk object you can access the feature objects (VpnAccount and VpnConnect);
this instance should be unique in your application.

```kotlin
val partnerConfiguration = PartnerConfiguration(
    apikey = "<YOUR_API_KEY>",
    accountName = "<YOUR_API_KEY>",
    authSuffix = "<YOUR_AUTH_SUFFIX or empty>",
    accountCreationKey = "<YOUR_API_KEY or empty>",
    overrideIkev2RemoteId = "<YOUR_IKEV2_REMOTE_ID or empty>" //This is optinal 
)

val vpnNotificationProvider = NotificationProvider(
    id = 1231, // The notification ID
    notification = notification// The instance of the android notification 
)

val revokedVpnNotificationProvider = NotificationProvider(
    id = 3213, // The notification ID
    notification = notification// The instance of the android notification 
)

val sdkConfiguration = SdkConfiguration(
    application = this,
    sdkConfiguration = SdkConfiguration(
        partnerConfiguration = partnerConfiguration,
        vpnNotificationProvider = vpnNotificationProvider,
        revokedVpnNotificationProvider = revokedVpnNotificationProvider,
    )
)

val vpnSdk = VpnSdk.init(
    application = application,
    sdkConfiguration = sdkConfiguration
) 
```

#### Customizing parameters

The SdkConfiguration class has build in default values of all of its parameters
(except the required ones).

You can call the constructor if you want to customize the parameters, you will have to input all of
the arguments in order to build the object.

If you just want to customize a couple of parameters and leave the others as default, is recommended
to use the `.copy()` method:

```kotlin
val sdkConfiguration = SdkConfiguration(
    application = this,
    sdkConfiguration = SdkConfiguration(
        partnerConfiguration = partnerConfiguration,
        vpnNotificationProvider = vpnNotificationProvider,
        revokedVpnNotificationProvider = revokedVpnNotificationProvider,
    )
).copy(
    clientConfiguration = it.clientConfiguration.copy(
        client = "Android",
        versionName = "x.x.x.x",
        isDebugBuild = BuildConfig.DEBUG
    ),
    vpnApi = it.vpnApi.copy(
        host = "https://vpnapi.com/api/vx/",
        mirrors = mirrorList,
    )
)
```

#### Accessing the instance

##### Application class

```kotlin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // ... sdk args init

        vpnSdk = VpnSdk.init(
            application = application,
            sdkConfiguration = sdkConfiguration
        )
    }

    // Use the companion object to create a single instance
    companion object {
        var vpnSdk: VpnSdk? = null
    }
}
```

##### DI (recommended)

Sample using Dagger/Hilt:

```kotlin
@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton // Single instance
    fun providesVpnSdk(): VpnSdk {
        // ... sdk args init
        return VpnSdk.init(
            application = application,
            sdkConfiguration = sdkConfiguration
        )
    }

    @Provides
    @Singleton // Single instance
    fun providesAccount(vpnSdk: VpnSdk): VpnAccount = vpnSdk.vpnAccount

    @Provides
    @Singleton // Single instance
    fun providesVpnConnection(vpnSdk: VpnSdk): VpnConnection = vpnSdk.vpnConnection
}

```

#### onVpnRestartTrigger listener

The SDK provides a `listener` lambda that you can implement to semi-automatically restart the 
VPN service when necessary. Since the SDK does not persist any VPN connection settings internally, 
this lambda serves as a useful trigger point for invoking `VpnConnection.connect(...)` with your own 
stored configuration.

This lambda is a suspend function, which means it's designed to work seamlessly within Kotlin 
coroutines you don't need to manage threading manually. Additionally, because it's defined as an 
extension function on `VpnConnection`, you can directly call any of its methods from within the 
lambda for a clean and concise implementation.

This trigger will be invoked under the following conditions:

- The VPN service is restarted due to system conditions like low memory or the service being 
killed unexpectedly.
- The user enables the Always-on VPN feature in the device’s system settings.
- The VPN service is reset.

Here’s an example of how to implement the listener:

```kotlin
vpnConnection.onVpnRestartTrigger {
    val myStoredSettings = .... // Get your stored settings
    connect(
        locationRequest = myStoredSettings.locationRequest 
        vpnProtocolSettings = myStoredSettings.vpnProtocolSettings
    )
}
```

## Documentation

1. [Changelog](WLVPN%20VPNSDK/CHANGELOG.md)
2. [Usage](WLVPN%20VPNSDK/USAGE.md)
3. [Architecture](WLVPN%20VPNSDK/ARCHITECTURE.md)
4. [Error Handling](WLVPN%20VPNSDK/ERROR%20HANDLING.md)
5. [Diagnostics Gathering](WLVPN%20VPNSDK/DIAGNOSTICS_GATHERING.md)
6. [Miscellaneous Tools](WLVPN%20VPNSDK/MISCELLANEOUS_TOOLS.md)
7. [Proguard Rules](WLVPN%20VPNSDK/PROGUARD.md)
8. [Token Based Authentication Document](common/TOKEN_BASED_AUTH.md)
9. [Threat Protection document](common/THREAT_PROTECTION.md)
