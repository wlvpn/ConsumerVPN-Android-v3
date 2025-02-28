package com.wlvpn.consumervpn.domain.gateway

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow

interface ExternalVpnSettingsGateway {

    fun fetchAvailableVpnPorts(
        protocolSettings: ProtocolSettings
    ): Flow<List<Int>>

    fun prepareThreatProtection(): Flow<Unit>

    class UnableToPrepareThreatProtectionFailure(val throwable: Throwable? = null) : Failure()
    class InvalidAccessTokenFailure : Failure()
    class InvalidApiKeyFailure : Failure()
    class ExpiredAccessTokenFailure : Failure()
    class ExpiredRefreshTokenFailure : Failure()

}