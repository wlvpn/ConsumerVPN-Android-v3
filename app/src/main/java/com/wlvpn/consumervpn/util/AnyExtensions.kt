package com.wlvpn.consumervpn.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal fun <T : Any> T.asFlow(): Flow<T> = flow {
    emit(this@asFlow)
}