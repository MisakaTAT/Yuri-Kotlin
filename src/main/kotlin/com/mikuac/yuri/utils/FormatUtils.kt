package com.mikuac.yuri.utils

import java.math.RoundingMode
import java.text.DecimalFormat

object FormatUtils {

    fun getNoMoreThanTwoDigits(number: Double): String {
        val format = DecimalFormat("0.##")
        // 未保留小数的舍弃规则 RoundingMode.FLOOR 表示直接舍弃
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

}