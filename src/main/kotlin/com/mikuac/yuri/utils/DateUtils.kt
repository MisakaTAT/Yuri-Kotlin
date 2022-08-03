package com.mikuac.yuri.utils

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

    }

}