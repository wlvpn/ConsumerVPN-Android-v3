# Split Tunnel Guide

This document provides comprehensive guidance on using split tunnel functionality in the VPN SDK v2.

## Table of Contents

1. [Split Tunnel Overview](#split-tunnel-overview)
2. [Split Tunnel Configuration](#split-tunnel-configuration)
3. [Quick Reference](#quick-reference)

## Split Tunnel Overview

Split tunneling allows you to configure apps, domains, or IP addresses to be excluded from the VPN 
tunnel. This enables you to route only specific traffic through the VPN connection while allowing 
other traffic to bypass the VPN and use the direct internet connection. 


### Supported Protocols

Split tunnel functionality is available for all VPN protocols:
- **WireGuard**: Supports both app and domain-based split tunneling
- **OpenVPN**: Supports both app and domain-based split tunneling  
- **IKEv2**: Supports both app and domain-based split tunneling

### Platform Requirements

Split tunneling support varies by Android version:
- **App-based split tunneling**: Requires Android 5.1+ (API level 22+)
- **Domain-based split tunneling**: Requires Android 13+ (API level 33+)

### Migration from SDK v1

In VPN SDK v2, the split tunneling configuration has been simplified and integrated into the protocol 
settings:

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
    splitTunnelApps = splitTunnelAppsList,
    splitTunnelDomains = splitTunnelDomainsList,
    // ... other settings
)
```

## Split Tunnel Configuration

### Basic Configuration Structure

All VPN protocol settings include split tunnel parameters:

```kotlin
// Common split tunnel properties for all protocols
val splitTunnelApps: List<String>        // List of app package names to exclude from VPN
val splitTunnelDomains: List<String>     // List of domains/IPs to exclude from VPN
```

**Supported Domain/IP Formats:**
- Domain names: `"example.com"`, `"www.google.com"`
- IPv4 addresses: `"192.168.1.1"`, `"10.0.0.1"`
- IPv6 addresses: `"2001:db8::1"`, `"fe80::1"`

### WireGuard Split Tunnel Configuration

```kotlin
val wireGuardSettings = VpnProtocolSettings.WireGuard(
    allowLan = true,
    splitTunnelApps = listOf(
        "com.google.android.youtube",
        "com.spotify.music",
        "com.netflix.mediaclient"
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

### OpenVPN Split Tunnel Configuration

```kotlin
val openVpnSettings = VpnProtocolSettings.OpenVpn(
    allowLan = true,
    splitTunnelApps = listOf(
        "com.banking.app",
        "com.work.email.client"
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
    splitTunnelApps = listOf(
        "com.microsoft.teams",
        "com.zoom.videomeetings"
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

**Domain-Based Split Tunneling:**
- Since the SDK does not perform validation on split tunnel inputs, consider implementing 
client-side validation
- IP addresses (IPv4/IPv6) are supported alongside domain names
- Some domains may use CDNs with different IP addresses

**Platform Limitations:**
- Android 5.0 and below: No app-based split tunneling support
- Android 12 and below: Limited domain-based split tunneling