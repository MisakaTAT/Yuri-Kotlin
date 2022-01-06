package com.mikuac.yuri.enums

class RegexCMD {

    companion object {
        const val BOT_STATUS = "(?i)status|状态"
        const val THROW_USER = "(?i)throw\\s(.*)|[丢抛]\\s(.*)"
        const val BV_AV_CONVERT = "(?i)bv[2转]av\\s(.*)|^(?i)av[2转]bv\\s(.*)"
        const val HTTP_CAT = "(?i)httpcat\\s([0-9]+)"
        const val PHOENIX_WRIGHT = "(?i)[逆n][转z][裁c][判p]\\s+(.*)\\s+(.*)"
        const val GITHUB_REPO = "(?i)github\\s(.*)"
        const val EPIC_FREE_GAME = "(?i)epic"
        const val SAUCE_NAO_SEARCH = "[搜识][图圖]"
        const val WHAT_ANIME_SEARCH = "[搜找]番"
        const val UNSET_SEARCH_MODE = "结束检索|谢谢"
        const val HELP = "(i?)help|帮助"
        const val ANIME_PIC = "(?i)setu(\\sr18)?|(?i)色图(\\sr18)?"
        const val BLOCK_USER = "(i?)ban\\s(.*)"
        const val UNBLOCK_USER = "(i?)unban\\s(.*)"
        const val TAROT = "(i?)tarot|塔罗牌"
        const val HITOKOTO = "(?i)hitokoto|一言"
        const val BANGUMI = "今日放送|(?i)bangumi"
    }

}

