package com.mikuac.yuri.common.utils

import com.mikuac.yuri.common.config.ReadConfig
import mu.KotlinLogging

class LogUtils {

    companion object {

        private val log = KotlinLogging.logger {}

        fun action(userId: Long, groupId: Long, pluginName: String) {
            val reqSource = if (groupId != 0L) "GROUP" else "PRIVATE"
            val text = if (groupId != 0L) "GroupID: $groupId UserID: $userId" else "UserID: $userId"
            log.info { "[$reqSource] $pluginName Execute - $text" }
        }

        fun debug(txt: String) {
            if (ReadConfig.config.base.debug) {
                log.error { "[DEBUG] $txt" }
            }
        }

    }

}