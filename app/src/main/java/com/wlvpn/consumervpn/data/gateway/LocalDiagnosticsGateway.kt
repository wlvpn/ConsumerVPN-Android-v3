package com.wlvpn.consumervpn.data.gateway

import android.content.Context
import com.wlvpn.consumervpn.domain.gateway.DiagnosticsGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

private const val CHILD_PATH_NAME = "log/diagnostics.txt"
private const val MAX_FILE_LINES = 1000

class LocalDiagnosticsGateway @Inject constructor(
    private val context: Context
) : DiagnosticsGateway {

    private val diagnosticsFile: File
        get() = File(context.filesDir, CHILD_PATH_NAME)

    override fun getDiagnosticsPath(): Flow<String> = flow {
        emit(diagnosticsFile.absolutePath)
    }

    override fun getDiagnostics(): Flow<List<String>> = flow {
        val diagnostics = if (diagnosticsFile.exists()) {
            // Memory safe read: caps memory footprint to the last 1000 lines
            diagnosticsFile.useLines { lines ->
                val buffer = java.util.LinkedList<String>()
                lines.forEach { line ->
                    buffer.add(line)
                    if (buffer.size > MAX_FILE_LINES) {
                        buffer.removeFirst()
                    }
                }
                buffer.toList()
            }
        } else {
            emptyList()
        }
        emit(diagnostics)
    }

    override fun clearDiagnostics(): Flow<Unit> = flow {
        if (diagnosticsFile.exists()) {
            diagnosticsFile.writeText("")
        }
        emit(Unit)
    }
}