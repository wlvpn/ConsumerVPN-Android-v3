# Split Tunnel Guide

This document provides comprehensive guidance on using split tunnel functionality in the VPN SDK v2.

## Table of Contents

1. [Split Tunnel Overview](#split-tunnel-overview)
2. [Split Tunnel Modes](#split-tunnel-modes)
3. [Split Tunnel Configuration](#split-tunnel-configuration)
4. [Quick Reference](#quick-reference)

## Split Tunnel Overview

Split tunneling lets you control which apps and which domains/IPs are routed through the VPN.
The SDK supports two app-based modes — *exclude selected apps from the VPN* and
*route only selected apps through the VPN* — as well as a domain/IP-based exclusion list.


### Supported Protocols

Split tunnel functionality is available for all VPN protocols:
- **WireGuard**: Supports both app-based modes and domain-based exclusion
- **OpenVPN**: Supports both app-based modes and domain-based exclusion
- **IKEv2**: Supports both app-based modes and domain-based exclusion

### Platform Requirements

Split tunneling support varies by Android version:
- **App-based split tunneling**: Requires Android 5.1+ (API level 22+)
- **Domain-based split tunneling**: Requires Android 13+ (API level 33+)

### Migration from SDK v1

In VPN SDK v2, the split tunneling configuration has been simplified and integrated into the
protocol settings:

**SDK v1 (Legacy)**:
```kotlin
VpnConnectionConfiguration
  .Builder(credentials.username, credentials.password)
  .splitTunnelApps(splitTunnelWhitelist)
  .domains(domainsWhitelist)
  .build()
```

**SDK v2 (Current)**:
```kotlin
VpnProtocolSettings.WireGuard(
    splitTunnelMode = SplitTunnelMode.DisallowedApps(splitTunnelAppsList),
    splitTunnelDomains = splitTunnelDomainsList,
    // ... other settings
)
```

## Split Tunnel Modes

App-based split tunneling is controlled by `SplitTunnelMode`, a sealed interface with three
states:

```kotlin
sealed interface SplitTunnelMode {

    data object Disabled : SplitTunnelMode

    data class DisallowedApps(val apps: List<String>) : SplitTunnelMode

    data class AllowedApps(val apps: List<String>) : SplitTunnelMode
}
```

| Mode | Behavior |
|---|---|
| `Disabled` | All app traffic routes through the VPN. Default. |
| `DisallowedApps(apps)` | Listed apps bypass the VPN; everything else routes through the VPN. |
| `AllowedApps(apps)` | Only listed apps route through the VPN; everything else bypasses the VPN. |

**Empty-list behavior:**
- `DisallowedApps(emptyList())` is equivalent to `Disabled` — all traffic routes through the VPN.
  Prefer `Disabled` explicitly for clarity at the call site.
- `AllowedApps(emptyList())` keeps its literal meaning: no app traffic routes through the VPN
  (only the SDK host package, which is always tunneled).

## Split Tunnel Configuration

### Basic Configuration Structure

All VPN protocol settings include split tunnel parameters:

```kotlin
val splitTunnelMode: SplitTunnelMode       // App-based mode (Disabled / DisallowedApps / AllowedApps)
val splitTunnelDomains: List<String>       // Domains/IPs to exclude from the VPN
```

**Supported Domain/IP Formats:**
- Domain names: `"example.com"`, `"www.google.com"`
- IPv4 addresses: `"192.168.1.1"`, `"10.0.0.1"`
- IPv6 addresses: `"2001:db8::1"`, `"fe80::1"`

### WireGuard Split Tunnel Configuration

```kotlin
val wireGuardSettings = VpnProtocolSettings.WireGuard(
    allowLan = true,
    splitTunnelMode = SplitTunnelMode.DisallowedApps(
        listOf(
            "com.google.android.youtube",
            "com.spotify.music",
            "com.netflix.mediaclient"
        )
    ),
    splitTunnelDomains = listOf(
        "www.netflix.com",
        "www.spotify.com",
        "api.local.company.com"
    ),
    dns = DnsSettings.Default,
    authMode = WireGuardAuthMode.BearerToken,
    multihopConnection = MultihopConnection.Disabled
)
```

### OpenVPN Split Tunnel Configuration — Reverse mode

```kotlin
val openVpnSettings = VpnProtocolSettings.OpenVpn(
    allowLan = true,
    splitTunnelMode = SplitTunnelMode.AllowedApps(
        listOf(
            "com.banking.app",
            "com.work.email.client"
        )
    ),
    splitTunnelDomains = listOf(
        "www.company.com",
        "internal.corporate.net",
        "localhost"
    ),
    dns = DnsSettings.Default,
    port = 443,
    internetProtocol = InternetProtocol.UDP,
    overrideMtu = false,
    reconnectOnDisconnect = true,
    isScrambleOn = false,
    configurationAttachments = emptyList(),
    useIpConnection = false,
    multihopConnection = MultihopConnection.Disabled
)
```

### IKEv2 Split Tunnel Configuration

```kotlin
val ikev2Settings = VpnProtocolSettings.IKEv2(
    allowLan = true,
    splitTunnelMode = SplitTunnelMode.DisallowedApps(
        listOf(
            "com.microsoft.teams",
            "com.zoom.videomeetings"
        )
    ),
    splitTunnelDomains = listOf(
        "www.teams.microsoft.com",
        "www.zoom.us",
        "meet.google.com"
    ),
    dns = DnsSettings.Default,
    useIpConnection = false
)
```


### Important Considerations

**App-Based Split Tunneling:**
- Package names must be exact (case-sensitive)
- Apps must be installed on the device to take effect
- System apps may behave differently than user apps
- `AllowedApps` (reverse split tunneling) restricts the VPN to only the listed apps — make sure
  the list includes every app that should reach the internet through the VPN

**Domain-Based Split Tunneling:**
- Since the SDK does not perform validation on split tunnel inputs, consider implementing
  client-side validation
- IP addresses (IPv4/IPv6) are supported alongside domain names
- Some domains may use CDNs with different IP addresses

**Platform Limitations:**
- Android 5.0 and below: No app-based split tunneling support
- Android 12 and below: Limited domain-based split tunneling