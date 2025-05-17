package com.wlvpn.consumervpn.presentation.controller

import android.app.Application
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToVpnContract
import com.wlvpn.vpnsdk.sdk.fetures.vpn.VpnConnection
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber

class VpnRestartController(
    application: Application,
    vpnConnection: VpnConnection,
    private val connectToVpnInteractor: ConnectToVpnContract.Interactor
) {

    init {
        vpnConnection.onVpnRestartTrigger {
            connectToVpnInteractor.execute()
                .firstOrNull()?.let {
                    Timber.i("VPN restart status $it")
                }
        }
    }
}