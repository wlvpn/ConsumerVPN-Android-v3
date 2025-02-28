package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Connected
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Connecting
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Disconnected
import com.wlvpn.consumervpn.domain.value.VpnConnectivityStatus.Error
import com.wlvpn.consumervpn.util.asFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ListenVpnStateInteractor(
    private val connectivityGateway: VpnConnectivityGateway
) : ListenVpnStateContract.Interactor {

    override fun execute(): Flow<Status> =
        connectivityGateway.listenToConnectStateChanges()
            .flatMapConcat {
                when (it) {
                    Connected -> connectivityGateway.getCurrentConnection()
                        .map { connection ->
                            Status.Connected(connection.server)
                        }

                    Connecting -> Status.Connecting.asFlow()
                    Disconnected -> Status.Disconnected.asFlow()
                    Error -> Status.VpnError.asFlow()
                }
            }
}