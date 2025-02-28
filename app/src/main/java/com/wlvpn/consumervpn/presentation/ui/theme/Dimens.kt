package com.wlvpn.consumervpn.presentation.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimens(
    val zero: Dp = 0.dp,
    val one: Dp = 1.dp,
    val xxxSmall: Dp = 2.dp,
    val xxSmall: Dp = 4.dp,
    val xSmall: Dp = 8.dp,
    val small: Dp = 12.dp,
    val normal: Dp = 16.dp,
    val medium: Dp = 20.dp,
    val large: Dp = 24.dp,
    val xLarge: Dp = 28.dp,
    val xxLarge: Dp = 32.dp,
    val xxxLarge: Dp = 36.dp,
    val wide: Dp = 40.dp,
    val xWide: Dp = 44.dp,
    val xxWide: Dp = 48.dp,
    val xxxWide: Dp = 52.dp,
)

val LocalDimens = compositionLocalOf { Dimens() }