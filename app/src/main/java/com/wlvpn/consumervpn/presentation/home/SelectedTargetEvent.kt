package com.wlvpn.consumervpn.presentation.home

import com.wlvpn.consumervpn.domain.value.ConnectionTarget

sealed class SelectedTargetEvent {
    data class SelectedTargetUpdated(val selectedTarget: ConnectionTarget) : SelectedTargetEvent()
}