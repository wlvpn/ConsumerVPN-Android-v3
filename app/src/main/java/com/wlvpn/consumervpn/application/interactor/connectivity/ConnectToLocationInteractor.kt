package com.wlvpn.consumervpn.application.interactor.connectivity

import com.wlvpn.consumervpn.application.interactor.connectivity.ConnectToLocationContract.Status
import com.wlvpn.consumervpn.domain.interactor.ConnectToSelectedServerContract
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.domain.value.ServerLocation.City
import com.wlvpn.consumervpn.domain.value.ServerLocation.Country
import com.wlvpn.consumervpn.domain.value.ServerLocation.Fastest
import com.wlvpn.consumervpn.domain.value.ServerLocation.Server
import com.wlvpn.consumervpn.util.catchOrEmpty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
class ConnectToLocationInteractor(
    private val connectToSelectedServerDomainInteractor:
    ConnectToSelectedServerContract.DomainInteractor,
    private val connectionSettingsRepository: ConnectionSettingsRepository
) : ConnectToLocationContract.Interactor {

    override fun execute(location: ServerLocation): Flow<Status> =
        connectionSettingsRepository.getConnectionSettings()
            .flatMapConcat {
                connectionSettingsRepository.saveConnectionSettings(
                    it.copy(
                        selectedTarget = when (location) {
                            is City -> ConnectionTarget.City(
                                country = ConnectionTarget.Country(
                                    location.country.code
                                ), name = location.name
                            )

                            is Country -> ConnectionTarget.Country(location.code)
                            Fastest -> ConnectionTarget.Fastest
                            is Server -> ConnectionTarget.Server(
                                city = ConnectionTarget.City(
                                    country = ConnectionTarget.Country(
                                        location.city.country.code
                                    ), name = location.city.name
                                ),
                                location.name
                            )
                        }
                    )
                )
            }.flatMapConcat {
                connectToSelectedServerDomainInteractor.execute()
                    .map {
                        when (it) {
                            ConnectToSelectedServerContract.Status.NoNetworkFailure ->
                                Status.NoNetworkFailure

                            ConnectToSelectedServerContract.Status.Success ->
                                Status.Success

                            is ConnectToSelectedServerContract.Status.UnableToConnectFailure -> {
                                Status.UnableToConnectFailure(it.reason)
                            }

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
}