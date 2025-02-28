package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.application.interactor.settings.SaveConnectionSettingsContract.Status
import com.wlvpn.consumervpn.domain.repository.ConnectionSettingsRepository
import com.wlvpn.consumervpn.domain.value.settings.ConnectionSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SaveConnectionSettingsInteractor(
    private val connectionSettingsRepository: ConnectionSettingsRepository,
) : SaveConnectionSettingsContract.Interactor {

    override fun execute(
        connectionsSettings: ConnectionSettings
    ): Flow<Status> =
         connectionSettingsRepository.saveConnectionSettings(connectionsSettings)
             .map { Status.Success as Status }
             .catch {
                emit(Status.UnableToSaveFailure)
            }
}