package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToVpnContract.Status
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract
import com.wlvpn.consumervpn.util.catchOrEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConnectToVpnInteractor(
    private val connectToSelectedServerDomainInteractor:
    ConnectToSelectedServerContract.DomainInteractor
) : ConnectToVpnContract.Interactor {

    override fun execute(): Flow<Status> =
        connectToSelectedServerDomainInteractor.execute()
            .map {
                when (it) {
                    ConnectToSelectedServerContract.Status.NoNetworkFailure ->
                        Status.NoNetworkFailure

                    ConnectToSelectedServerContract.Status.Success ->
                        Status.Success

                    is ConnectToSelectedServerContract.Status.UnableToConnectFailure ->
                        Status.UnableToConnectFailure(it.reason)

                    ConnectToSelectedServerContract.Status.UserNotLoggedFailure ->
                        Status.UserNotLoggedFailure

                    ConnectToSelectedServerContract.Status.VpnNotPreparedFailure ->
                        Status.VpnNotPreparedFailure

                    is ConnectToSelectedServerContract.Status.VpnServiceFailure ->
                        Status.InvalidWireGuardApiResponseFailure(it.errorCode)

                    ConnectToSelectedServerContract.Status.InvalidatedAccountFailure ->
                        Status.InactiveWireGuardAccountFailure

                    ConnectToSelectedServerContract.Status.ExpiredAccountFailure ->
                        Status.ExpiredWireGuardAccountFailure
                }
            }.catchOrEmpty {
                emit(Status.UnableToConnectFailure(it?.localizedMessage))
            }
}