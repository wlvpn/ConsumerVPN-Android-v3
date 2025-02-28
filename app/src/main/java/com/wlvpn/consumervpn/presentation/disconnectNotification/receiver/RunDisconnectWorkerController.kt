package com.wlvpn.consumervpn.presentation.disconnectNotification.receiver

import com.wlvpn.consumervpn.application.interactor.connectivity.DisconnectFromVpnContract
import com.wlvpn.consumervpn.presentation.disconnectNotification.receiver.RunDisconnectWorkerContract.Receiver
import kotlinx.coroutines.flow.first

class RunDisconnectWorkerController(
    private val disconnectFromVpnInteractor: DisconnectFromVpnContract.Interactor
) : RunDisconnectWorkerContract.Controller {

    var receiver: Receiver? = null

    override suspend fun syncStart() {
        disconnectFromVpnInteractor.execute().first()
    }

    override fun bindReceiver(receiver: Receiver) {
        this.receiver = receiver
    }

    override fun unbindReceiver() {
        receiver = null
    }
}