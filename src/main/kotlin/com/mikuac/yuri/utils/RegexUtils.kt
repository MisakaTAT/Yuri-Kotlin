package com.mikuac.yuri.utils

object RegexUtils {

    fun group(group: String, text: String, regex: String): String {
        val match = regex.toRegex().find(text) ?: return ""
        return match.groups[group]?.value ?: ""
    }

    fun group(group: Int, text: String, regex: String): String {
        val match = regex.toRegex().find(text) ?: return ""
        val groups = match.groupValues
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