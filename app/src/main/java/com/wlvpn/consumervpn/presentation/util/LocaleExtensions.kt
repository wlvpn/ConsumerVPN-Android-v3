package com.wlvpn.consumervpn.presentation.util

import java.util.Locale

fun Locale.countryNameFromCode(countryCode: String): String =
    Locale("", countryCode).getDisplayCountry(this)