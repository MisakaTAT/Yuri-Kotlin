package com.mikuac.yuri.utils

import com.mikuac.yuri.config.ReadConfig
import mu.KotlinLogging

object LogUtils {

    private val log = KotlinLogging.logger {}

    fun debug(txt: String) {
        if (ReadConfig.config.base.debug) {
            log.error { "[DEBUG] $txt" }
        }
    }

}