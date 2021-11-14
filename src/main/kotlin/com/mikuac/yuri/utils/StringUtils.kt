package com.mikuac.yuri.utils

object StringUtils {

    fun unescape(string: String): String {
        return string.replace("&#44;", ",")
            .replace("&#91;", "[")
            .replace("&#93;", "]")
            .replace("&amp;", "&")
    }

}