package com.wlvpn.consumervpn.presentation.startup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import timber.log.Timber

private const val ACTION_QUICK_BOOT_POWER_ON = "android.intent.action.QUICKBOOT_POWERON"
private const val ACTION_HTC_QUICK_BOOT_POWER_ON = "com.htc.intent.action.QUICKBOOT_POWERON"

class AutoStartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let { action ->
            if (action.isActionBootCompleted) {
                val async = goAsync()
                Intent(context, AutoStartupService::class.java).run {
                    this.action = AutoStartupService.ACTION_START
                    if (context != null) {
                        ContextCompat.startForegroundService(context, this)
                        Timber.i("startForegroundService invoked...")
                    } else {
                        Timber.e("Failed to invoke the start foreground service...")
                    }
                }
                async.finish()
            } else {
                // ignore
            }
        }
    }

    private val String.isActionBootCompleted: Boolean
        get() {
            return this.equals(Intent.ACTION_BOOT_COMPLETED, true)
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    && this.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED, true))
                    || this.equals(ACTION_QUICK_BOOT_POWER_ON, true)
                    || this.equals(ACTION_HTC_QUICK_BOOT_POWER_ON, true)
        }
}