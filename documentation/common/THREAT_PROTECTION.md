<!-- TOC -->

- [Overview](#overview)
- [Setup](#setup)
- [Usage](#usage)

<!-- TOC -->

# Overview

Threat Protection feature ensures a secure user traffic by blocking malicious ads through the
VPN DNS.

This document is a guide on how to use it in your app.

# Setup

To enable threat protection you first need to fetch the configuration needed to run this feature
calling:

```kotlin
...
VpnConnection.prepareThreatProtection()
    .collect { response ->
        when (response) {
            Success -> {
                // Successful setup
            }

            else -> {
                // Failed to setup see `PrepareThreatProtectionResponse` for more details
            }
        }
    }
```

This call returns a `PrepareThreatProtectionResponse` object which describes if the call was
successful or failed for any reason (see [PrepareThreatProtectionResponse](../sdk/src/main/java/com/wlvpn/vpnsdk/sdk/fetures/vpn/VpnConnection.kt))

Is recommended to call it once per user session.

# Usage

To enable Threat Protection in a VPN connection you simply enable through the `ProtocolSettings`