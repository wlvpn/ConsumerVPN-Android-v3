package com.wlvpn.consumervpn.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEmpty

internal fun <T> Flow<T>.catchOrEmpty(
    block: suspend FlowCollector<T>.(Throwable?) -> Unit
): Flow<T> =
    onEmpty { block(null) }
        .catch { block(it) }

/**
 * This operator will consume the first item in the flow and will repeat
 * the consumption while the condition is meet.
 */
fun <T> Flow<T>.repeatFirstUntil(
    shouldRepeat: suspend FlowCollector<T>.(T, iteration: Int) -> Boolean
): Flow<T> = flow {
    val originalFlow = this@repeatFirstUntil
    var iteration = 1
    do {
        var value = originalFlow.first()
        val repeat = shouldRepeat(value, iteration++)

        if (!repeat) {
            emit(value)
        }
    } while (repeat)
}