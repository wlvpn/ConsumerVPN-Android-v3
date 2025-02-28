package com.wlvpn.consumervpn.presentation.home

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.painterResource
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.wlvpn.consumervpn.presentation.home.MenuButtonState.Disabled
import com.wlvpn.consumervpn.presentation.home.MenuButtonState.Highlighted
import com.wlvpn.consumervpn.presentation.home.MenuButtonState.Normal
import com.wlvpn.consumervpn.presentation.home.MenuButtonState.Selected
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.LocalDimens

@Composable
fun MenuButton(
    modifier: Modifier,
    icon: Int,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    if (isEnabled) {
        var settingsState: MenuButtonState by remember { mutableStateOf(MenuButtonState.Normal) }
        IconButton(
            modifier = modifier.onFocusEvent {
                settingsState = if (it.isFocused) {
                    Highlighted
                } else {
                    Normal
                }
            },
            onClick = {
                settingsState = Selected
                onClick()
            },
            colors = IconButtonDefaults.colors(
                containerColor = LocalColors.current.scheme.inverseOnSurface,
                focusedContainerColor = LocalColors.current.extendedColors.homeMenuButtonSelectedColor,
                pressedContainerColor = LocalColors.current.extendedColors.homeMenuButtonSelectedColor,
                disabledContainerColor = LocalColors.current.scheme.inverseOnSurface,
            )
        ) {
            Icon(
                modifier = Modifier.size(LocalDimens.current.xWide),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = when (settingsState) {
                    Disabled -> LocalColors.current.scheme.surfaceVariant
                    Highlighted -> LocalColors.current.scheme.primaryContainer
                    Normal -> LocalColors.current.scheme.onSurface
                    Selected -> LocalColors.current.scheme.primaryContainer
                }
            )
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(100),
            colors = SurfaceDefaults.colors(
                containerColor = LocalColors.current.scheme.inverseOnSurface,
            )
        ) {
            Icon(
                modifier = Modifier
                    .size(LocalDimens.current.xWide)
                    .align(Alignment.Center),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = LocalColors.current.scheme.surfaceVariant
            )
        }
    }
}

sealed interface MenuButtonState {
    data object Normal : MenuButtonState
    data object Disabled : MenuButtonState
    data object Highlighted : MenuButtonState
    data object Selected : MenuButtonState
}