package com.wlvpn.consumervpn.domain.failure

const val EMPTY_FAILURE_MESSAGE = ""

open class Failure(
    message: String = EMPTY_FAILURE_MESSAGE,
    throwable: Throwable? = null
) : Throwable(message, throwable)