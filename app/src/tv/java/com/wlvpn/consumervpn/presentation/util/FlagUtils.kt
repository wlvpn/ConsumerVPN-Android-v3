package com.wlvpn.consumervpn.presentation.util

import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import com.wlvpn.consumervpn.BuildConfig
import com.wlvpn.consumervpn.domain.value.ServerLocation
import java.util.Locale

private const val TV_SMALL_SIZE_FLAG_LOCATION = "320x220/_flag_"
private const val TV_LARGE_SIZE_FLAG_LOCATION = "640x440/_flag_"
private const val IMAGE_FILE_FORMAT = ".png"

fun ServerLocation.Country.getUriForFlag(context: Context): Uri {
    val density = context.resources.displayMetrics.densityDpi

    val flagDensity = when {
        density <= DisplayMetrics.DENSITY_TV -> FlagResDensity1.LOW
        else -> FlagResDensity1.HIGH
    }

    val flagPrefix = BuildConfig.FLAGS_HOSTNAME + flagDensity.densityText
    val fixedCountryCode = code.lowercase(Locale.ENGLISH)
    return Uri.parse(flagPrefix + fixedCountryCode + IMAGE_FILE_FORMAT)
}

private enum class FlagResDensity1(val densityText: String) {
    LOW(TV_SMALL_SIZE_FLAG_LOCATION),
    HIGH(TV_LARGE_SIZE_FLAG_LOCATION),
}