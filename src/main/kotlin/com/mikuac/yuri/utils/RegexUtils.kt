package com.mikuac.yuri.utils

class RegexUtils {
    companion object {
        fun group(regex: Regex, group: Int, txt: String): String {
            return regex.findAll(txt).toList().flatMap(MatchResult::groupValues)[group]
        }

    }
}