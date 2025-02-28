package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.application.interactor.settings.SaveProtocolSettingsContract.Status
import com.wlvpn.consumervpn.domain.repository.ProtocolSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.ProtocolSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SaveProtocolSettingsInteractor(
    private val protocolSettingsRepository: ProtocolSettingsRepository,
) : SaveProtocolSettingsContract.Interactor {

    override fun execute(
        protocolSettings: ProtocolSettings
    ): Flow<Status> =
        protocolSettingsRepository.saveProtocolSettings(protocolSettings)
            .map {
                Status.Success as Status
            }.catch {
                emit(Status.UnableToCompleteFailure)
            }
}