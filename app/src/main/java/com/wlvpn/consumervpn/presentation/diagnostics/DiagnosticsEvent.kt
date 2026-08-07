package com.wlvpn.consumervpn.presentation.diagnostics

sealed class DiagnosticsEvent {
    object LoadingInProgress : DiagnosticsEvent()
    object Error : DiagnosticsEvent()

    data class DiagnosticsLoaded(
        val diagnostics: List<String> = emptyList(),
        val sharePath: String? = null
    ) : DiagnosticsEvent()
}