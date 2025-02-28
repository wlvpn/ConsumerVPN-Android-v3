package com.wlvpn.consumervpn.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/* Used to send string resources and dynamic strings received from the Api to the UI layer */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(val resId: Int, vararg val args: Any) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is StringResource -> stringResource(id = resId, args)
            is DynamicString -> value
        }
    }
}
