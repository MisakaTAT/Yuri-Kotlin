package com.mikuac.yuri.common.utils

class RegexUtils {
    companion object {

        fun find(regex: Regex, txt: String): String? {
            return regex.matchEntire(txt)?.value
        }

        fun group(regex: Regex, group: Int, txt: String): String {
            return regex.findAll(txt).toList().flatMap(MatchResult::groupValues)[group]
        }

    }
}