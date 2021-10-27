package com.mikuac.yuri.common.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class DateUtils {

    companion object {

        fun sToMS(value: Double): String {
            val minute = value.roundToInt() / 60
            val seconds = value.roundToInt() % 60
            val m = minute.toFloat().roundToInt()
            val s = seconds.toFloat().roundToInt()
            return m.toString() + "分" + s + "秒"
        }

        fun getTime(): String {
            val time = LocalDateTime.now()
            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            return time.format(format)
        }

    }

}