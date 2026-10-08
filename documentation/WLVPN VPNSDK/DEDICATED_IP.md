# Dedicated IP Servers

Dedicated IP servers provide dedicated IP addresses assigned exclusively to a user's account.
This feature is only available for accounts that have dedicated IPs provisioned.

1. [Prerequisites](#prerequisites)
2. [Checking dedicated IP entitlement](#checking-dedicated-ip-entitlement)
3. [Fetching dedicated servers](#fetching-dedicated-servers)
4. [Finding dedicated servers](#finding-dedicated-servers)
    1. [All dedicated servers](#all-dedicated-servers)
    2. [By country](#by-country)
    3. [By city](#by-city)
    4. [By server name](#by-server-name)
    5. [Filtering by server features](#filtering-by-server-features)
5. [Reading the dedicated IP address](#reading-the-dedicated-ip-address)
6. [Connecting to a dedicated IP server](#connecting-to-a-dedicated-ip-server)
7. [Custom endpoint configuration](#custom-endpoint-configuration)

## Prerequisites

Dedicated IP availability is determined automatically by the SDK during login. The login flow
internally fetches the API configuration and, if the account has dedicated IPs, fetches and caches
the dedicated server list. No additional setup is needed beyond a successful login.

## Checking dedicated IP entitlement

After logging in, you can check whether the account has dedicated IP support using
`vpnAccount.getUserEntitlements()`. If the account has dedicated IPs, the entitlements set
will contain `UserEntitlements.DedicatedIp`.

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    vpnAccount.getUserEntitlements()
        .map { response ->
            when (response) {
                is GetUserEntitlementsResponse.Success -> {
                    val hasDedicatedIp = response.entitlements
                        .contains(UserEntitlements.DedicatedIp)
                    // Use hasDedicatedIp to show/hide dedicated IP UI
                }

                is GetUserEntitlementsResponse.NoApiConfigurationFound -> {
                    // API configuration not yet available
                    // Make sure the user is logged in first
                }

                is GetUserEntitlementsResponse.UnableToGetUserEntitlements -> {
                    // Handle error
                }
            }
        }.first()
}
```

## Fetching dedicated servers

Dedicated servers are fetched and cached locally as part of the login flow. Once cached, they are
available for offline queries just like the regular server list.

The dedicated server list is refreshed automatically whenever the SDK performs a token refresh
(`vpnAccount.refreshToken()`). The token refresh triggers an internal configuration update that
re-fetches dedicated servers if the account supports them.

> **Note:** `vpnConnection.updateServers()` only updates the regular server list. It does **not**
> fetch or update dedicated servers.

## Finding dedicated servers

Use `vpnConnection.findServers()` with `dedicatedServers = true` on any `FindServerOptions`
variant. The response is the same `FindServersResponse` sealed class used for regular servers.

### All dedicated servers

```kotlin
val allDedicatedServers: List<Location.Server> =
    vpnConnection.findServers(
        options = FindServerOptions.All(dedicatedServers = true)
    ).map { response ->
        when (response) {
            is FindServersResponse.Success -> response.servers

            else -> emptyList()
        }
    }.first()
```

### By country

```kotlin
val country: Location.Country = // ... fetch the country with findCountries(...)

val dedicatedServersInCountry: List<Location.Server> =
    vpnConnection.findServers(
        options = FindServerOptions.ByCountry(
            country = country,
            dedicatedServers = true
        )
    ).map { response ->
        when (response) {
            is FindServersResponse.Success -> response.servers

            else -> emptyList()
        }
    }.first()
```

### By city

```kotlin
val city: Location.City = // ... fetch the city with findCities(...)

val dedicatedServersInCity: List<Location.Server> =
    vpnConnection.findServers(
        options = FindServerOptions.ByCity(
            city = city,
            dedicatedServers = true
        )
    ).map { response ->
        when (response) {
            is FindServersResponse.Success -> response.servers

            else -> emptyList()
        }
    }.first()
```

### By server name

```kotlin
val dedicatedServersByName: List<Location.Server> =
    vpnConnection.findServers(
        options = FindServerOptions.ByName(
            name = "server-name",
            dedicatedServers = true
        )
    ).map { response ->
        when (response) {
            is FindServersResponse.Success -> response.servers

            else -> emptyList()
        }
    }.first()
```

### Filtering by server features

The `ByName`, `ByCountry`, and `ByCity` options accept a `features` parameter to filter dedicated
servers by `ServerFeature`. For example, to find only RAM-only dedicated servers in a country:

```kotlin
val ramOnlyDedicatedServers: List<Location.Server> =
    vpnConnection.findServers(
        options = FindServerOptions.ByCountry(
            country = country,
            features = setOf(ServerFeature.RamOnly),
            dedicatedServers = true
        )
    ).map { response ->
        when (response) {
            is FindServersResponse.Success -> response.servers

            else -> emptyList()
        }
    }.first()
```

## Reading the dedicated IP address

Each `Location.Server` has a `dedicatedIp` field with the dedicated IP address assigned to
the account. For regular servers this field is `null`.

```kotlin
val server: Location.Server = // ... from findServers() result

// The dedicated IP assigned to this account on this server
val dedicatedIp: String? = server.dedicatedIp

if (dedicatedIp != null) {
    println("Dedicated IP: $dedicatedIp")
}
```

## Connecting to a dedicated IP server

Connecting to a dedicated IP server works exactly the same as connecting to a regular server.
All `LocationRequest` variants accept a `dedicatedServers` parameter to indicate that the
connection should target a dedicated server.

### By location

```kotlin
class ConnectionViewModel(
    val vpnConnection: VpnConnection,
) : ViewModel() {

    val vpnProtocolSettings = // ... see USAGE.md for protocol settings setup

    fun connectToDedicatedServer(server: Location.Server) {
        viewModelScope.launch(Dispatchers.IO) {
            vpnConnection.connectToVpn(
                locationRequest = LocationRequest.ByLocation(server),
                vpnProtocolSettings = vpnProtocolSettings
            ).map { response ->
                when (response) {
                    ConnectToVpnResponse.Success -> {
                        // Connected to dedicated IP server
                    }

                    else -> {
                        // Handle connection error
                    }
                }
            }.first()
        }
    }
}
```

### By server name

```kotlin
vpnConnection.connectToVpn(
    locationRequest = LocationRequest.ByServerName(
        serverName = "server-name",
        dedicatedServers = true
    ),
    vpnProtocolSettings = vpnProtocolSettings
).map { response ->
    when (response) {
        ConnectToVpnResponse.Success -> {
            // Connected to dedicated IP server
        }

        else -> {
            // Handle connection error
        }
    }
}.first()
```

## Custom endpoint configuration

The dedicated servers endpoint defaults to `"servers/private-ip"`. You can override it through
`VpnApi.dedicatedServersEndpoint` when setting up the SDK:

```kotlin
val sdkConfiguration = SdkConfiguration(
    partnerConfiguration = partnerConfiguration,
    vpnNotificationProvider = vpnNotificationProvider,
    revokedVpnNotificationProvider = revokedVpnNotificationProvider,
    vpnApi = VpnApi(
        // ... other endpoint configurations ...
        dedicatedServersEndpoint = "custom/dedicated-ip/endpoint",
        // ...
    )
)
```

Or, if you want to override only the dedicated servers endpoint while keeping defaults for
everything else, use `copy()`:

```kotlin
val sdkConfiguration = SdkConfiguration(
    partnerConfiguration = partnerConfiguration,
    vpnNotificationProvider = vpnNotificationProvider,
    revokedVpnNotificationProvider = revokedVpnNotificationProvider,
).copy(
    vpnApi = defaultVpnApi.copy(
        dedicatedServersEndpoint = "custom/dedicated-ip/endpoint"
    )
)
```