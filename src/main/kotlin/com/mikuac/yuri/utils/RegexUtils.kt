package com.mikuac.yuri.utils

object RegexUtils {

    fun group(group: String, txt: String, regex: String): String {
        return regex.toRegex().matchEntire(txt)?.groups?.get(group)?.value ?: ""
    }

    fun group(group: Int, txt: String, regex: String): String {
        val matchResult = regex.toRegex().find(txt) ?: return ""
        val groups = matchResult.groupValues
        return if (group in groups.indices) groups[group] else ""
    }

    fun check(text: String, regex: String): Boolean {
        val pattern = regex.toRegex()
        return pattern.containsMatchIn(text)
    }

    fun match(text: String, regex: String): Boolean {
        return regex.toRegex().matches(text)
    }

}