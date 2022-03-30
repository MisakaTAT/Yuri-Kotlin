package com.mikuac.yuri.utils

import java.math.RoundingMode
import java.text.DecimalFormat

object FormatUtils {

    fun getNoMoreThanTwoDigits(number: Double): String {
        val df = DecimalFormat("#.##")
        // 未保留小数的舍弃规则 RoundingMode.FLOOR 表示直接舍弃
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number)
    }

}