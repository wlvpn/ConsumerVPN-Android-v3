package com.wlvpn.consumervpn.presentation.controller

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.Connected
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.Connecting
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.Disconnected
import com.wlvpn.consumervpn.application.interactor.connectivity.ListenVpnStateContract.Status.VpnError
import com.wlvpn.consumervpn.presentation.notifications.NotificationFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val VPN_STATE_DELAY_MILLIS = 200L

class NotificationController(
    private val application: Application,
    private val notificationManager: NotificationManager,
    private val listenVpnStateInteractor: ListenVpnStateContract.Interactor,
    private val notificationFactory: NotificationFactory
) {

    private val notificationId = application.resources.getInteger(R.integer.vpn_notification_id)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                application.getString(R.string.notification_channel_id),
                application.getString(R.string.notification_channel_label_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = application.getString(R.string.notification_channel_label_description)
                enableLights(false)
                setShowBadge(false)
                notificationManager.createNotificationChannel(this)
            }
        }

        MainScope().launch(Dispatchers.IO) {
            listenVpnStateInteractor.execute()
                .onEach { delay(VPN_STATE_DELAY_MILLIS) }
                .collect {
                    when (it) {
                        is Connected -> notificationManager.notify(
                            notificationId,
                            notificationFactory.connectedNotification(it.server)
                        )

                        Connecting -> notificationManager.notify(
                            notificationId,
                            notificationFactory.connectingNotification
                        )

                        Disconnected,
                        VpnError ->
                            notificationManager.cancel(notificationId)
                    }
                }
        }
    }
}