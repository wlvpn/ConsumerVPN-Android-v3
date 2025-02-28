package com.wlvpn.consumervpn.application.interactor.location

import com.wlvpn.consumervpn.application.interactor.location.RetrieveCityLocationsContract.Status
import com.wlvpn.consumervpn.domain.gateway.ExternalServersGateway
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.zip

class RetrieveCityLocationsInteractor(
    private val serverGateway: ExternalServersGateway,
    private val connectionSettingsRepository: ConnectionSettingsRepository,
) : RetrieveCityLocationsContract.Interactor {

    override fun execute(): Flow<Status> =
        serverGateway.retrieveCityLocations()
            .zip(
                connectionSettingsRepository.getConnectionSettings()
            ) { cities, settings ->
                Status.Success(
                    cityLocationsList = cities,
                    settings.selectedTarget
                ) as Status
            }
            .catch { throwable ->
                when (throwable) {
                    is ExternalServersGateway.UnableToRetrieveCityServerLocationsFailure ->
                        emit(Status.UnableToRetrieveCityLocationsFailure)

                    else -> throw throwable
                }
            }
}