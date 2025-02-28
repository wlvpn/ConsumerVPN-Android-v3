package com.wlvpn.consumervpn.presentation.disconnectNotification.receiver

interface RunDisconnectWorkerContract {

    interface Controller {

        suspend fun syncStart()
        fun bindReceiver(receiver: Receiver)
        fun unbindReceiver()
    }

    interface Receiver
}