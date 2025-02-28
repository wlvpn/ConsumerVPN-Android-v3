package com.wlvpn.consumervpn.domain.gateway

import com.wlvpn.consumervpn.domain.failure.Failure
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.CurrentConnection
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow

interface VpnConnectivityGateway {

    fun connect(
        target: ConnectionTarget,
        protocolSettings: ProtocolSettings,
        connectionSettings: ConnectionSettings,
    ): Flow<Unit>

    fun disconnect(): Flow<Unit>
    fun isVpnConnected(): Flow<Boolean>
    fun isVpnPrepared(): Flow<Boolean>
    fun listenToConnectStateChanges(): Flow<VpnConnectivityStatus>
    fun getCurrentConnection(): Flow<CurrentConnection>
    fun currentVpnState(): Flow<VpnConnectivityStatus>

    // Failures
    class NotConnectedToVpnServerFailure : Failure()
    class InternalServerFailure : Failure()
    class NetworkErrorFailure : Failure()
    class InvalidatedAccountFailure : Failure()
    class ExpiredAccountFailure : Failure()
    class UserNotLoggedInFailure : Failure()
    class FetchNearestServerFailure : Failure()
    data class VpnServiceFailure(val code: Int, val reason: String? = null) : Failure()
    data class VpnConnectionFailure(val throwable: Throwable?) : Failure()
}