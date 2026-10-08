package com.wlvpn.consumervpn.domain.gateway

import kotlinx.coroutines.flow.Flow

interface DiagnosticsGateway {
    fun getDiagnosticsPath(): Flow<String>
    fun getDiagnostics(): Flow<List<String>>
    fun clearDiagnostics(): Flow<Unit>
}