package com.mikuac.yuri.utils

import mu.KotlinLogging

object LogUtils {

    private val log = KotlinLogging.logger {}

    fun error(txt: String) {
        log.error { txt }
    }

}