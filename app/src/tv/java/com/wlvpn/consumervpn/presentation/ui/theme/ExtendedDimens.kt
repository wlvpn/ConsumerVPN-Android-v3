package com.wlvpn.consumervpn.presentation.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ExtendedDimens(
    val none: Dp = 0.dp,
    val homeScreenConnectLogoPadding: Dp = 64.dp,
    val homeScreenConnectedLogoSize: Dp = 92.dp,
    val homeScreenLocationPadding: Dp = 64.dp,
    val homeScreenConnectingContainerPadding: Dp = 90.dp,
    val homeScreenConnectivityButtonPadding: Dp = 80.dp,
    val homeScreenIpRowPadding: Dp = 220.dp,
    val homeScreenIpRowSpacerSize: Dp = 180.dp,
    val homeScreenMenuRowPadding: Dp = 38.dp,
    val homeScreenProgressIndicatorSize: Dp = 130.dp,
    val homeScreenProgressStrokeSize: Dp = 110.dp,
    val homeScreenProgressStrokeWidth: Dp = 2.dp,
    val homeScreenIpLabelFontSize: TextUnit = 16.sp,
    val homeScreenIpFontSize: TextUnit = 20.sp,
    val homeScreenLocationFontSize: TextUnit = 20.sp,
    val homeScreenMenuButtonSize: Dp = 64.dp,
    val homeScreenConnectivityButtonSize: Dp = 300.dp,
    val loginScreenPortraitPadding: Dp = 64.dp,
    val loginScreenLogoPadding: Dp = 90.dp,
    val loginScreenLabelPadding: Dp = 280.dp,
    val loginScreenForgotPasswordButtonHeight: Dp = 380.dp,
    val loginScreenButtonHeight: Dp = 50.dp,
    val loginScreenCircularIndicatorStrokeWidth: Dp = 3.dp,
    val forgotPasswordScreenQrCodeImageSize: Dp = 200.dp,
    val forgotPasswordScreenButtonSize: Dp = 100.dp,

    //locations screen
    val locationsScreenTitleFontSize: TextUnit = 30.sp,
    val locationsScreenFastestAvailableBoxPadding: Dp = 100.dp,
    val locationsScreenLabelFontSize: TextUnit = 12.sp,
    val locationsScreenFastestAvailableLabelLineHeight: TextUnit = 16.sp,
    val locationsScreenRowItemSize: Dp = 130.dp,
    val locationsScreenFlagIconHeight: Dp = 80.dp,

    val settingsTitleSize: TextUnit= 18.sp,
    val settingsItemDescriptionSize: TextUnit= 12.sp,
    val settingsItemTitleSize: TextUnit= 14.sp,
    val settingsSectionTitleSize: TextUnit= 16.sp,
    val settingsDialogTitleSpacing: TextUnit = 2.sp,
    val settingsDialogTitleLineHeight: TextUnit = 48.sp,
    val settingsDialogTitleSize: TextUnit = 38.sp,
    val settingsDialogDescriptionSize: TextUnit = 18.sp,
    val settingsDialogRadioTextSize: TextUnit = 14.sp,

    //about us screen
    val aboutUsDialogButtonTitleSize: TextUnit = 10.sp,
    val aboutUsScreenQrCodeImageSize: Dp = 200.dp,
    val aboutUsScreenButtonSize: Dp = 100.dp,

    val splashScreenLogoSize: Dp = 380.dp,

    val webViewDialogUrlFontSize: TextUnit = 18.sp
)

val Dimens.extended: ExtendedDimens
    get() = ExtendedDimens()

