package com.mikuac.yuri.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

object DateUtils {

    fun sToMS(value: Double): String {
        val minute = value.roundToInt() / 60
        val seconds = value.roundToInt() % 60
        val m = minute.toFloat().roundToInt()
        val s = seconds.toFloat().roundToInt()
        return m.toString() + "分" + s + "秒"
    }

    fun format(date: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return date.format(formatter)
    }

    fun format(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }

}