package com.wlvpn.consumervpn.application.interactor.settings

import com.wlvpn.consumervpn.application.interactor.settings.PrepareThreatProtectionContract.Status
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway.ExpiredAccessTokenFailure
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway.ExpiredRefreshTokenFailure
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway.InvalidAccessTokenFailure
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway.InvalidApiKeyFailure
import com.wlvpn.consumervpn.domain.gateway.ExternalVpnSettingsGateway.UnableToPrepareThreatProtectionFailure
import com.wlvpn.consumervpn.util.catchOrEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrepareThreatProtectionInteractor(
    private val externalVpnSettingsGateway: ExternalVpnSettingsGateway
) : PrepareThreatProtectionContract.Interactor {

    override fun execute(): Flow<Status> =
        externalVpnSettingsGateway.prepareThreatProtection()
            .map { Status.Success as Status }
            .catchOrEmpty {
                when (it) {
                    is InvalidAccessTokenFailure,
                    is InvalidApiKeyFailure,
                    is ExpiredAccessTokenFailure,
                    is ExpiredRefreshTokenFailure ->
                        emit(Status.NotAuthorizedFailure)

                    is UnableToPrepareThreatProtectionFailure ->
                        emit(Status.UnableToPrepareThreatProtectionFailure(it.throwable))

                    else -> emit(Status.UnableToPrepareThreatProtectionFailure(it))
                }
            }
}