package com.wlvpn.consumervpn.data.gateway.logs

import android.util.Log
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import timber.log.Timber

private val fileLogger: Logger = LoggerFactory.getLogger(ConsumerReleaseTree::class.java)

class ConsumerReleaseTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean =
        !(priority == Log.DEBUG || priority == Log.VERBOSE)

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        if (isLoggable(tag, priority)) {
            val messageWithTag = if (tag != null) "[$tag] $message" else message

            Log.println(priority, tag, message)
            when (priority) {
                Log.INFO -> fileLogger.info(messageWithTag)
                Log.WARN -> fileLogger.warn(messageWithTag)
                Log.ERROR -> fileLogger.error(messageWithTag)
                Log.ASSERT -> fileLogger.warn(messageWithTag)
                else -> {
                }
            }
        }
    }
}