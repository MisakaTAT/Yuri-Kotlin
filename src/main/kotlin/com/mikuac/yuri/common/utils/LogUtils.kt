package com.mikuac.yuri.common.utils

import com.mikuac.yuri.common.config.ReadConfig
import mu.KotlinLogging

class LogUtils {

    companion object {

        private val log = KotlinLogging.logger {}

        fun debug(txt: String) {
            if (ReadConfig.config.base.debug) {
                log.warn { "DEBUG: $txt" }
            }
        }

    }

}