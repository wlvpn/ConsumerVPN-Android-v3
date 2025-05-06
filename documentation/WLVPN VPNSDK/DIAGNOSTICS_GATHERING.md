# Diagnostics Gathering

The SDK uses [Timber][1] as logging utility.

## `Timber library`

This is a logger with a small, extensible API which provides utility on top of
Android's normal Log class.

### `Usage`

- Add the dependency to the project

In your project root level `build.gradle` file, add this line to your
`allprojects` block:

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

Edit your module-level `build.gradle` file and add your new dependency:

```groovy
dependencies {
    implementation 'com.jakewharton.timber:timber:5.0.1'
}
```

## Enabling SDK logs

After the dependency has been added you only need 2 simple steps to use the logger:
Install any Tree instances you want in the onCreate of your application class.

```kotlin
import android.app.Application
import timber.log.Timber

class ExampleApp : Application() {
    fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    /** A tree which diagnostics important information for crash reporting.  */
    private class CrashReportingTree : Tree() {
        override fun log(priority: Int, tag: String?, @NonNull message: String,
        t: Throwable) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            FakeCrashLibrary.log(priority, tag, message)
            if (t != null) {
                if (priority == Log.ERROR) {
                    FakeCrashLibrary.logError(t)
                } else if (priority == Log.WARN) {
                    FakeCrashLibrary.logWarning(t)
                }
            }
        }
    }
}

//FakeCrashLibrary
/** Not a real crash reporting library!  */
class FakeCrashLibrary private constructor() {
    init {
        throw AssertionError("No instances.")
    }

    companion object {
        fun log(priority: Int, tag: String?, message: String?) {
            // TODO add log entry to circular buffer.
        }

        fun logWarning(t: Throwable) {
            // TODO report non-fatal warning.
        }

        fun logError(t: Throwable) {
            // TODO report non-fatal error.
        }
    }
}
```

## Identifying log messages from theSDK

By default the SDK will log information with the tag "[VPN-SDK]".

## Setting a custom tag for the SDK

The SDKConfig object can be used to pass a custom tag for the SDK.

```
val sdkConfiguration = SdkConfiguration(
    vkpSdkLogTag = "CustomTag"
)

val vpnSdk = VpnSdk.init(
    application = application,
    sdkConfiguration = sdkConfiguration
)
```

## Obtaining only logs from the SDK

If it is desired to capture only the logs from the SDK a custom Timber
tree can be used.

```
import timber.log.Timber

class TaggedTimberTree(private val allowedTag: String) : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String,
    t: Throwable) {
        if (tag != null && tag == allowedTag) {
            // Log only if the tag matches the allowed tag
            when (priority) {
                Log.VERBOSE -> Timber.tag(tag).v(message)
                Log.DEBUG -> Timber.tag(tag).d(message)
                Log.INFO -> Timber.tag(tag).i(message)
                Log.WARN -> Timber.tag(tag).w(message)
                Log.ERROR -> Timber.tag(tag).e(t, message)
                Log.ASSERT -> Timber.tag(tag).wtf(message)
            }
        }
    }
}

val customTag = "customSDKTag"

val sdkConfiguration = SdkConfiguration(
    vkpSdkLogTag = customTag
)

val vpnSdk = VpnSdk.init(
    application = application,
    sdkConfiguration = sdkConfiguration
)

// Or use [VPN-SDK] to use the default tag value.
Timber.plant(TaggedTimberTree(customTag))
```

## Removing the logs of the SDK

If it is desired to remove the logs from the SDK a custom Timber tree can be used.

```
import timber.log.Timber

class FilteredTimberTree(private val filteredTag: String) : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String,
    t: Throwable) {
        if (tag != null && tag != filteredTag) {
            // Log only if the tag is not filtered
            when (priority) {
                Log.VERBOSE -> Timber.tag(tag).v(message)
                Log.DEBUG -> Timber.tag(tag).d(message)
                Log.INFO -> Timber.tag(tag).i(message)
                Log.WARN -> Timber.tag(tag).w(message)
                Log.ERROR -> Timber.tag(tag).e(t, message)
                Log.ASSERT -> Timber.tag(tag).wtf(message)
            }
        }
    }
}

val customTag = "customSDKTag"

val sdkConfiguration = SdkConfiguration(
    vkpSdkLogTag = customTag
)

val vpnSdk = VpnSdk.init(
    application = application,
    sdkConfiguration = sdkConfiguration
)

// Or use [VPN-SDK] to use the default tag value.
Timber.plant(FilteredTimberTree(customTag))
```

**Note:**
If the client application does not use Timber as logging utility then simply
by not planting any Timber tree the logging of the SDK will not be shown.

[1]: https://github.com/JakeWharton/timber
