package com.wlvpn.consumervpn.presentation.startup

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.wlvpn.consumervpn.R
import com.wlvpn.consumervpn.R.integer
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectOnBootContract
import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectOnBootContract.Status.Success
import com.wlvpn.consumervpn.presentation.di.module.BASE_NOTIFICATION_KEY
import com.wlvpn.consumervpn.util.catchOrEmpty
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class AutoStartupService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var connectOnBootInteractor: ConnectOnBootContract.Interactor

    @Inject
    @Named(BASE_NOTIFICATION_KEY)
    lateinit var baseNotification: NotificationCompat.Builder

    override fun onBind(intent: Intent?): IBinder? {
        throw RuntimeException("Service is not bind-able")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            //Note: When Intent is null Android is recreating the service
            // after memory was available again
            null, ACTION_START -> {
                showForegroundNotification()
                connect()
                START_STICKY
            }

            else -> super.onStartCommand(intent, flags, startId)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun showForegroundNotification() {
        val id = application.resources.getInteger(integer.vpn_notification_id)
        val notification = baseNotification
            .setContentTitle(application.getString(R.string.notification_vpn_label_title_starting))
            .setSmallIcon(R.drawable.notification_icon)
            .build()

        ServiceCompat.startForeground(
            this,
            id,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
    }

    private fun connect() = scope.launch {
        val response = connectOnBootInteractor.execute()
            .catchOrEmpty { }
            .firstOrNull() ?: run {
            stopSelf()
            return@launch
        }

        when (response) {
            Success ->
                Timber.i("Connected on boot")

            else ->
                Timber.e("error starting connect on boot")
        }

        stopSelf()
    }

    companion object {

        const val ACTION_START = "START"
    }
}