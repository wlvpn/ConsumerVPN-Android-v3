package com.wlvpn.consumervpn.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

const val TABLET_DP_SIZE = 600

@Composable
fun isTablet(): Boolean {
    return LocalConfiguration.current.screenWidthDp >= TABLET_DP_SIZE
}