package com.wlvpn.consumervpn.presentation

import android.app.Application
import com.wlvpn.consumervpn.BuildConfig
import com.wlvpn.consumervpn.data.gateway.logs.ConsumerDebugTree
import com.wlvpn.consumervpn.data.gateway.logs.ConsumerReleaseTree
import com.wlvpn.consumervpn.presentation.controller.NotificationController
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ConsumerApplication : Application() {

    @Inject
    lateinit var notificationController: NotificationController

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(ConsumerDebugTree())
        } else {
            Timber.plant(ConsumerReleaseTree())
        }
    }
}