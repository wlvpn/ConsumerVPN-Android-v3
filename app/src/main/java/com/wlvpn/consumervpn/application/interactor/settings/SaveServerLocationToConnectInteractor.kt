package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.application.interactor.settings.SaveServerLocationToConnectContract.Status
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.ConnectionTarget
import com.wlvpn.consumervpn.domain.value.ServerLocation
import com.wlvpn.consumervpn.domain.value.ServerLocation.City
import com.wlvpn.consumervpn.domain.value.ServerLocation.Country
import com.wlvpn.consumervpn.domain.value.ServerLocation.Fastest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty

@OptIn(ExperimentalCoroutinesApi::class)
class SaveServerLocationToConnectInteractor(
    private val connectionSettingsRepository: ConnectionSettingsRepository
) : SaveServerLocationToConnectContract.Interactor {

    override fun execute(serverLocation: ServerLocation): Flow<Status> =
        connectionSettingsRepository.getConnectionSettings()
            .flatMapConcat {
                connectionSettingsRepository.saveConnectionSettings(
                    it.copy(selectedTarget = serverLocation.asTarget)
                )
            }.map {
                Status.Success as Status
            }.onEmpty {
               emit(Status.UnableToSaveServerLocation())
            }.catch {
                emit(Status.UnableToSaveServerLocation())
            }
}

private val ServerLocation.asTarget
    get() =
        when (this) {
            is City ->
                ConnectionTarget.City(
                    country = ConnectionTarget.Country(code = country.code),
                    name = name
                )

            is Country -> ConnectionTarget.Country(code = code)

            Fastest -> ConnectionTarget.Fastest

            is ServerLocation.Server -> ConnectionTarget.Server(
                city = ConnectionTarget.City(
                    country = ConnectionTarget.Country(code = city.country.code),
                    name = city.name
                ), name = name
            )
        }