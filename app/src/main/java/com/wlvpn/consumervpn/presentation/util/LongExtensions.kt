package com.wlvpn.consumervpn.presentation.util

import java.text.SimpleDateFormat
import java.util.Locale

private const val DATE_FORMAT = "dd/MM/yyyy HH:mm"

internal fun Long.toReadableDate(): String =
    SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(this)