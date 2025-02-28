package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.application.interactor.connectivity.DisconnectFromVpnContract.Status
import com.wlvpn.consumervpn.domain.gateway.VpnConnectivityGateway
import com.wlvpn.consumervpn.util.catchOrEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DisconnectFromVpnInteractor @Inject constructor(
    private val connectivityGateway: VpnConnectivityGateway
) : DisconnectFromVpnContract.Interactor {

    override fun execute(): Flow<Status> =
        connectivityGateway.disconnect()
            .map {
                Status.Success
            }.catchOrEmpty {
                Status.UnableDisconnectFailure(it?.localizedMessage)
            }
}