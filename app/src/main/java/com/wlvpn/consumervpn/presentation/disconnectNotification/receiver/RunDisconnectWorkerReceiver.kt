package com.wlvpn.consumervpn.presentation.disconnectNotification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RunDisconnectWorkerReceiver : BroadcastReceiver(), RunDisconnectWorkerContract.Receiver {
    companion object {

        const val ACTION_DISCONNECT_VPN = "com.wlvpn.consumervpn.ACTION_DISCONNECT_VPN"
        const val REQUEST_CODE = 42721
    }

    @Inject
    lateinit var controller: RunDisconnectWorkerContract.Controller
    private lateinit var mContext: Context

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            // Run Disconnect worker
            if (it.action == ACTION_DISCONNECT_VPN) {
                controller.bindReceiver(this)
                val disconnectIntent = goAsync()
                GlobalScope.launch {
                    controller.syncStart()
                    disconnectIntent.finish()
                }
                controller.unbindReceiver()
                // Unregister this receiver
                context?.let { context ->
                    mContext = context
                    context.unregisterReceiver(this)
                }
            }
        }
    }
}