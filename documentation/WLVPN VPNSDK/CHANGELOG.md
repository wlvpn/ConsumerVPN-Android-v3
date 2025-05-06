# VPN SDK Changelog

## VPN SDK v2.3.7

### New Items

- VPN service now correctly restarts after the OS kills it due low memory or any other unexpected 
scenario.

### Breaking Changes

- `VpnConnection.alwaysOnTrigger(...)`  changed to `VpnConnection.onVpnRestartTrigger(...)`
- The lambda input for `VpnConnection.onVpnRestartTrigger(...)` is now a suspend lambda and
  is an extension of `VpnConnection` which, with this change is not needed to manage any threading
  inside the lambda.

## VPN SDK v2.3.6

### New Items
- Adds a new SDK configuration to allow client to set a fallback Geo location:
```kotlin
VpnSdk.setup(
    ... //configuration,
   geoLocationFallback = GeoLocationFallback(
     countryCode = "US",
     latitude = 39.8283,
     longitude = -98.5795,
   )
)
```

## VPN SDK v2.3.5

### New Items
- Adds `Detected API mirror failure` documentation

## VPN SDK v2.3.4

## Improvements
- Fixes an edge case with WireGuard not reporting correctly unexpected errors that could crash the app.
- Adds missing URL queries to the API mirrors.

## VPN SDK v2.3.3
- Proguard Rules for WireGuard:
    ```proguard
    -keep class com.wlvpn.wireguard.android.backend.GoBackend {
      public static void onNotifyHandshakeFailureCallback(int);
    }
    ```

### Improvements
- Pass allowlan parameter to Wireguard endpoint.
- Removes the forced background disconnect when a possible port block is detected.
- Fixes and edge case when notifying a handshake failure could generate a crash.
- Upgrades API version from v3.3 to v3.4.

### New Items
- Added a PotentialServiceDenial VPN state to reflect a repeated handshake failure scenarios. The implementer need to review its own VPN State mapping to match their intent on this new state, this state might be triggered also when the network connectivity is interrupted
- Added an option to skip the logout api call:
  ```kotlin
  vpnAccount.lougout(skipApiCall = true).collect{...}
  ```
- Added `X-API-Key:<API key>` to the servers API call.

## VPN SDK v2.3.2

### Improvements
- Improves always-on compatibility for OpenVPN, it'll also restart the VPN service if the application is restarted by the system.
- Fixes an issue where on refresh token expiration `ExpiredRefreshTokenFailure` wasn't being properly propagated.
- Fixed WireGuard crash when an invalid domain is used inside the split tunneling feature.
- Improved error consistency across all methods that interact with the WLVPN API.
- Fixes an edge case where the connection feature could return an empty flow.
- Improves refresh token management.

### New Items
- Introduces a new response for the `VpnConnection.connect` feature: 
  - `ConnectToVpnResponse.PossiblePortBlockedFailure`: Returned if the local network is possibly blocking the necessary ports to have a successful connection.(Only for WireGuard)

## VPN SDK v2.3.1

### New Items
- The VPN SDK is no longer obfuscated : The obfuscation needs to be done by the implementer. [See obfuscation rules](/documentation/PROGUARD.md).
- VPN disconnection after new VPN Connections the VPN SDK will disconnect of any active VPN connection before starting a new one.

### Improvements
- Mitigated ANRs thrown by IKEv2 protocol.
- All throwables included in error responses are no longer optional.

### Breaking Changes
- The VPN SDK is no longer obfuscated.

# VPN SDK Changelog

## VPN SDK v2.3.0

### New Items
- Optimize `VpnConnection.updateServers`: The method now uses API 3.3, which returns a smaller response.
- Updated `FeatureCompatibility`: Server list API selection used by `VpnConnection.updateServers` (previously called `oldScheduleMaintenance`). You can now select between versions.
- Proguard Rules for IKEv2:
    ```proguard
    -keep class org.strongswan.android.logic.CharonVpnService {
      private static java.lang.String getAndroidVersion();
      private static java.lang.String getDeviceString();
      private byte[][] getTrustedCertificates();
      public void updateStatus(int);
      public void updateImcState(int);
      public void addRemediationInstruction(java.lang.String);
      private byte[][] getTrustedCertificates();
      private byte[][] getUserCertificate();
      private java.security.PrivateKey getUserKey();
      native <methods>;
    }
    -keep class org.strongswan.android.logic.NetworkManager {
      <init>(android.content.Context);
      public void Register();
      public void Unregister();
    }
    -keep class org.strongswan.android.logic.CharonVpnService$BuilderAdapter {
      public synchronized <methods>;
    }
    ```
- Added `TooManyRequests` response to the `VpnConnection.connection` and `VpnConnection.fetchGeoLocation` features.

### Improvements
- Fixed incorrect Innactive account messages on VPN connections
- Improved API fallback mechanism 

### Breaking Changes
- None.

## VPN SDK v2.2.0

### New Items
- Multihop connection; this will allow the user's connection to enter through one city and exit through another.

### Breaking Changes
- None.

## VPN SDK v2.1.0

### New Items
- Added `getOpenVpnAvailablePorts()`: This method returns the available ports for the OpenVPN protocol, used to update the UI with the OpenVPN available ports or to create a connection request.
    ```kotlin
    val ports = getOpenVpnAvailablePorts()
                .map { if (it is Success) it.ports else emptyList() }
                .first()

    val scramblePorts = getOpenVpnAvailablePorts()
                       .map { if (it is Success) it.scramblePorts else emptyList() }
                       .first()
    ```
- Added `overrideIkev2RemoteId` option to the `PartnerConfiguration` settings. This should be left as default.
- Added `DnsSettings.Partner` to the DNS settings.
- Added metadata management methods in `VpnAccount`:
  - `getCollectionMetadata`
  - `putCollectionMetadata`
  - `clearCollectionMetadata`
- Added `LoginRequest.WithCredentials` and `LoginRequest.WithToken`, enabling token-based authentication.
- Added more relevant errors when the API key, access token, or refresh token are invalid/expired.

### Removed Items
- Removed the `updateProtocols()` method. No protocol update is required anymore.
- Removed `getOpenVpnInfo()`, `getIkev2Info()`, and `getWireGuardInfo()` methods. These methods only returned relevant data for OpenVPN. Replaced by `getOpenVpnAvailablePorts()`.
- Removed `forceWGServerSupport` and `manualIkev2RemoteId` options from `FeatureCompatibility`, these are now fully integrated into the common flow.

### Breaking Changes
- Removed fetch protocols endpoint call to reduce the initial setup complexity.
- Deprecated methods removed. Update implementations to use the new methods provided.

## VPN SDK v2.0

### Breaking Changes
- Initial version with major updates and changes that set the foundation for subsequent versions.
