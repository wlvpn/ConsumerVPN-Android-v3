package com.wlvpn.consumervpn.presentation.di.module

import com.wlvpn.consumervpn.application.interactor.connectivity.DisconnectFromVpnInteractor
import com.wlvpn.consumervpn.presentation.disconnectNotification.receiver.RunDisconnectWorkerContract
import com.wlvpn.consumervpn.presentation.disconnectNotification.receiver.RunDisconnectWorkerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class ControllerModule {

    @Provides
    fun providesRunDisconnectWorkerController(
        disconnectFromVpnInteractor: DisconnectFromVpnInteractor
    ): RunDisconnectWorkerContract.Controller =
        RunDisconnectWorkerController(disconnectFromVpnInteractor)
}