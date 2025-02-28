package com.wlvpn.consumervpn.presentation.util

import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import com.wlvpn.consumervpn.BuildConfig
import com.wlvpn.consumervpn.domain.value.ServerLocation
import java.util.Locale

private const val SMALL_SIZE_FLAG_LOCATION = "50x34/_flag_"
private const val MEDIUM_SIZE_FLAG_LOCATION = "66x44/_flag_"
private const val LARGE_SIZE_FLAG_LOCATION = "640x440/_flag_"
private const val IMAGE_FILE_FORMAT = ".png"

fun ServerLocation.Country.getUriForFlag(context: Context): Uri {
    val density = context.resources.displayMetrics.densityDpi

    val flagDensity = when {
        density <= DisplayMetrics.DENSITY_TV -> FlagResDensity.LOW
        density >= DisplayMetrics.DENSITY_XHIGH -> FlagResDensity.HIGH
        else -> FlagResDensity.HIGH
    }
    val flagPrefix = BuildConfig.FLAGS_HOSTNAME + flagDensity.densityText
    val fixedCountryCode = code.lowercase(Locale.ENGLISH)
    return Uri.parse(flagPrefix + fixedCountryCode + IMAGE_FILE_FORMAT)
}

private enum class FlagResDensity(val densityText: String) {
    LOW(SMALL_SIZE_FLAG_LOCATION),
    MEDIUM(MEDIUM_SIZE_FLAG_LOCATION),
    HIGH(LARGE_SIZE_FLAG_LOCATION),
}