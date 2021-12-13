package com.mikuac.yuri.enums

enum class RegexEnum(val value: Regex) {
    HELP("(?i)help|帮助".toRegex()),
    HTTP_CAT("(?i)httpcat\\s([0-9]+)".toRegex()),
    BOT_STATUS("^(?i)status|^[状狀][态態]".toRegex()),
    GITHUB_REPO("^(?i)github\\s(-p)?+\\s?(.*)\$".toRegex()),
    BV_TO_AV("^(?i)bv[2转]av\\s(.*)|^(?i)av[2转]bv\\s(.*)".toRegex()),
    WHAT_ANIME_SET("^[搜识找識]([索别])?番([剧劇])?(模式)?".toRegex()),
    WHAT_ANIME_UNSET("^退出[搜识找識]([索别])?番([剧劇])?(模式)?".toRegex()),
    SAUCE_NAO_SET("^[搜识找識]([索别])?[图圖本推]([子特])?(模式)?".toRegex()),
    SAUCE_NAO_UNSET("^退出[搜识找識]([索别])?[图圖本推]([子特])?(模式)?".toRegex()),
    EROTIC_PIC("^[来來发發给給]([1一])?[张張个個幅点點份]([Rr]18的?)?[色瑟][图圖]|^setu(\\s[Rr]18)?|^[色瑟][图圖](\\s[Rr]18)?".toRegex()),
    EPIC("^(?i)epic(免费游戏)?".toRegex())
}