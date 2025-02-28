package com.wlvpn.consumervpn.data.gateway

import android.content.Context
import android.net.VpnService
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.ExpiredAccountFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.FetchNearestServerFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.InternalServerFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.InvalidatedAccountFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.NetworkErrorFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.NotConnectedToVpnServerFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.UserNotLoggedInFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.VpnConnectionFailure
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway.VpnServiceFailure
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.CurrentConnection
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.City
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.Country
import com.wlvpn.consumervpn.domain.value.ConnectionTarget.Fastest
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import com.wlvpn.consumervpn.domain.value.settings.InternetProtocol
import com.wlvpn.consumervpn.domain.value.settings.OpenVpnPort
import com.wlvpn.consumervpn.domain.value.settings.Protocol
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import com.wlvpn.consumervpn.util.asFlow
import com.wlvpn.vpnsdk.domain.value.ConnectionInfo
import com.wlvpn.vpnsdk.domain.value.ConnectionInfo.CurrentMultihopConnection
import com.wlvpn.vpnsdk.domain.value.ConnectionInfo.NotConnected
import com.wlvpn.vpnsdk.domain.value.DnsSettings
import com.wlvpn.vpnsdk.domain.value.FindCityOptions.ByName
import com.wlvpn.vpnsdk.domain.value.FindCountryOptions.ByCountryCode
import com.wlvpn.vpnsdk.domain.value.FindServerOptions
import com.wlvpn.vpnsdk.domain.value.InternetProtocol as SdkInternetProtocol
import com.wlvpn.vpnsdk.domain.value.Location
import com.wlvpn.vpnsdk.domain.value.Location.Nearest
import com.wlvpn.vpnsdk.domain.value.LocationRequest
import com.wlvpn.vpnsdk.domain.value.MultihopConnection
import com.wlvpn.vpnsdk.domain.value.VpnProtocol.IKEv2
import com.wlvpn.vpnsdk.domain.value.VpnProtocol.OpenVpn
import com.wlvpn.vpnsdk.domain.value.VpnProtocol.WireGuard
import com.wlvpn.vpnsdk.domain.value.VpnProtocolSettings
import com.wlvpn.vpnsdk.domain.value.VpnState
import com.wlvpn.vpnsdk.domain.value.WireGuardAuthMode
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.ConnectToIKEv2Failure
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.ConnectToOpenVpnFailure
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.ConnectToWireGuardFailure
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.ExpiredAccount
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.InvalidatedAccount
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.NoBestServerFound
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.ServiceFailure
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectToVpnResponse.UserNotLoggedIn
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectionInfoResponse
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ConnectionInfoResponse.UnableToGetConnectionInfo
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.FindCitiesResponse
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.FindCountriesResponse
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.FindServersResponse
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ListenVpnStateResponse
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ListenVpnStateResponse.Success
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection.ListenVpnStateResponse.UnableToListenVpnState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take

@OptIn(ExperimentalCoroutinesApi::class)
class VpnSdkConnectivityGateway(
    private val context: Context,
    private val vpnConnection: VpnConnection,
) : VpnConnectivityGateway {

    override fun connect(
        target: ConnectionTarget,
        protocolSettings: ProtocolSettings,
        connectionSettings: ConnectionSettings
    ): Flow<Unit> =
        when (target) {
            is City ->
                vpnConnection.findCities(ByName(target.country.code, target.name))
                    .filter { it is FindCitiesResponse.Success }
                    .map { (it as FindCitiesResponse.Success).cities.first() as Location }

            is Country ->
                vpnConnection.findCountries(ByCountryCode(target.code))
                    .filter { it is FindCountriesResponse.Success }
                    .map { (it as FindCountriesResponse.Success).countries.first() }

            Fastest -> flow { emit(Nearest) }

            is ConnectionTarget.Server ->
                vpnConnection.findServers(FindServerOptions.ByName(target.name))
                    .filter { it is FindServersResponse.Success }
                    .map { (it as FindServersResponse.Success).servers.first() }

        }.flatMapConcat { location ->
            vpnConnection.connectToVpn(
                locationRequest = LocationRequest.ByLocation(location),
                vpnProtocolSettings = when (protocolSettings) {
                    is ProtocolSettings.IKEv2 -> VpnProtocolSettings.IKEv2(
                        allowLan = protocolSettings.allowLan,
                        splitTunnelApps = emptyList(),
                        splitTunnelDomains = emptyList(),
                        dns = if (connectionSettings.isThreatProtectionEnabled) {
                            DnsSettings.Protected
                        } else {
                            DnsSettings.Default
                        },
                        useIpConnection = false
                    )

                    is ProtocolSettings.OpenVpn -> VpnProtocolSettings.OpenVpn(
                        allowLan = protocolSettings.allowLan,
                        splitTunnelApps = emptyList(),
                        splitTunnelDomains = emptyList(),
                        dns = if (connectionSettings.isThreatProtectionEnabled) {
                            DnsSettings.Protected
                        } else {
                            DnsSettings.Default
                        },
                        port = when (val port = protocolSettings.port) {
                            is OpenVpnPort.Normal -> port.value
                            is OpenVpnPort.Scramble -> port.value
                        },
                        internetProtocol = when (protocolSettings.internetProtocol) {
                            InternetProtocol.TCP -> SdkInternetProtocol.TCP
                            InternetProtocol.UDP -> SdkInternetProtocol.UDP
                        },
                        overrideMtu = protocolSettings.overrideMtu,
                        reconnectOnDisconnect = true,
                        isScrambleOn = protocolSettings.scramble,
                        configurationAttachments = emptyList(),
                        useIpConnection = false,
                        multihopConnection = MultihopConnection.Disabled
                    )

                    is ProtocolSettings.Wireguard -> VpnProtocolSettings.WireGuard(
                        allowLan = protocolSettings.allowLan,
                        splitTunnelApps = emptyList(),
                        splitTunnelDomains = emptyList(),
                        dns = if (connectionSettings.isThreatProtectionEnabled) {
                            DnsSettings.Protected
                        } else {
                            DnsSettings.Default
                        },
                        authMode = WireGuardAuthMode.BearerToken,
                        multihopConnection = MultihopConnection.Disabled
                    )
                }
            )
        }.flatMapConcat {
            when (it) {
                ConnectToVpnResponse.Success -> Unit.asFlow()

                UserNotLoggedIn -> throw UserNotLoggedInFailure()

                NoBestServerFound -> throw FetchNearestServerFailure()

                is ConnectToIKEv2Failure -> throw VpnConnectionFailure(it.throwable)

                is ConnectToOpenVpnFailure -> throw VpnConnectionFailure(it.throwable)

                is ConnectToWireGuardFailure -> throw VpnConnectionFailure(it.throwable)

                ExpiredAccount -> throw ExpiredAccountFailure()

                InvalidatedAccount -> throw InvalidatedAccountFailure()

                ConnectToVpnResponse.NotConnected -> throw NetworkErrorFailure()

                is ServiceFailure -> throw VpnServiceFailure(it.code, it.reason)

                else -> throw VpnServiceFailure(0, it::class.java.simpleName)

            }
        }

    override fun disconnect(): Flow<Unit> = vpnConnection.disconnectFromVpn()
        .flatMapConcat { Unit.asFlow() }

    override fun isVpnConnected(): Flow<Boolean> =
        vpnConnection.listenVpnState()
            .take(1)
            .map {
                it is ListenVpnStateResponse.Success && it.vpnState is VpnState.Connected
            }

    override fun isVpnPrepared(): Flow<Boolean> = flow {
        emit(VpnService.prepare(context) == null)
    }

    override fun listenToConnectStateChanges(): Flow<VpnConnectivityStatus> =
        vpnConnection.listenVpnState()
            .map {
                when (it) {
                    is Success -> it.vpnState.toLocalState()
                    is UnableToListenVpnState -> throw InternalServerFailure()
                }
            }

    override fun getCurrentConnection(): Flow<CurrentConnection> =
        vpnConnection.getConnectionInfo()
            .map {
                when (it) {
                    is ConnectionInfoResponse.Success ->
                        when (val connectionInfo = it.connectionInfo) {
                            is ConnectionInfo.CurrentConnection -> connectionInfo
                            NotConnected -> throw NetworkErrorFailure()
                            is CurrentMultihopConnection -> throw NotConnectedToVpnServerFailure()
                        }

                    is UnableToGetConnectionInfo ->
                        throw NotConnectedToVpnServerFailure()
                }.let { connection ->
                    CurrentConnection(
                        timeConnected = connection.connectionStartTime,
                        server = connection.server.toLocalServer(),
                        protocol = when (connection.vpnProtocol) {
                            WireGuard -> Protocol.WireGuard
                            OpenVpn -> Protocol.OpenVpn
                            IKEv2 -> Protocol.IKEv2
                        }

                    )
                }
            }

    override fun currentVpnState(): Flow<VpnConnectivityStatus> =
        vpnConnection.listenVpnState()
            .take(1)
            .map {
                when (it) {
                    is Success -> it.vpnState.toLocalState()
                    is UnableToListenVpnState ->
                        throw NotConnectedToVpnServerFailure()
                }
            }

    private fun VpnState.toLocalState(): VpnConnectivityStatus =
        when (this) {
            is VpnState.Connected -> VpnConnectivityStatus.Connected
            is VpnState.DisconnectedError -> VpnConnectivityStatus.Error
            is VpnState.Connecting -> VpnConnectivityStatus.Connecting
            is VpnState.Disconnected -> VpnConnectivityStatus.Disconnected
        }

    private fun Location.Server.toLocalServer(): ServerLocation.Server =
        ServerLocation.Server(
            city = ServerLocation.City(
                country = ServerLocation.Country(
                    name = city.country.name,
                    code = city.country.code,
                    cities = emptyList()
                ), name = city.name
            ),
            name = name,
            load = capacity
        )
}