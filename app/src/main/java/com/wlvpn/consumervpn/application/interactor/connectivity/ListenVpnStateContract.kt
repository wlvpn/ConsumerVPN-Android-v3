package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.domain.value.ServerLocation
import kotlinx.coroutines.flow.Flow

interface ListenVpnStateContract {

    interface Interactor {

        fun execute(): Flow<Status>
    }

    sealed class Status {

        object Connecting : Status()

        data class Connected(val server: ServerLocation.Server? = null) : Status()

        object Disconnected : Status()

        // NOTE: This is not a failure, is the vpn state, this will not
        // interrupt the flow of this interactor
        object VpnError : Status()
    }
}