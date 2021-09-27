package com.mikuac.yuri.common.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Slf4j {
    companion object {
        val <reified T> T.log: Logger
            inline get() = LoggerFactory.getLogger(T::class.java)
    }
}