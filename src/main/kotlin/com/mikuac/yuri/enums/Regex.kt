package com.mikuac.yuri.enums

@Suppress("SpellCheckingInspection")
object Regex {

    const val CHAT_GPT = "^(?i)chat\\s(?<action>set|del|show)?\\s?(?<prompt>[\\s\\S]+?)?\$"
    const val DOU_YIN_REAL_URL_ID = "/video/(?<id>\\d+)"
    const val DOU_YIN_SHORT_URL = "(?<url>(?:https?:\\/\\/)?v\\.douyin\\.com\\/\\w+)"
    const val BILIBILI_BID = "(?<=video/)(.*)(?=/\\?)"
    const val YOUTUBE_URL = "^(?:https?://)?(?:www.)?(?:youtube.com|youtu.be)/(?:watch\\?v=)([^#&?]*).*\$"

    const val BOT_STATUS = "^(?i)status\$|^状态\$"
    const val THROW_USER = "^(?i)throw\\s(.*)\$|^[丢抛]\\s(.*)\$"
    const val BV_AV_CONVERT = "^(?i)bv[2转]av\\s(.*)\$|^(?i)av[2转]bv\\s(.*)\$"
    const val HTTP_CAT = "^(?i)httpcat\\s([0-9]+)\$"
    const val PHOENIX_WRIGHT = "^(?i)[逆n][转z][裁c][判p]\\s+(.*)\\s+(.*)\$"
    const val GITHUB_REPO = "^(?i)github\\s(.*)\$"
    const val EPIC_FREE_GAME = "^(?i)epic\$"
    const val SAUCE_NAO_SEARCH = "^[搜识][图圖]\$"
    const val WHAT_ANIME_SEARCH = "^[搜找]番\$"
    const val UNSET_SEARCH_MODE = "^结束检索|谢谢\$"
    const val HELP = "^(?i)help\$|^帮助\$"
    const val SETU = "^(?i)setu(\\sr18)?\$|(?i)色图(\\sr18)?\$"
    const val TAROT = "^(?i)tarot\$|^塔罗牌\$"
    const val HITOKOTO = "^(?i)hitokoto\$|^一言\$"
    const val ANIME_CRAWLER = "^(今日放送)\$|^(星期|周)([1-7]|[一二三四五六七日天])番剧\$"
    const val Huobi = "^(?i)bc\\s([a-z]+)\$"
    const val R6S = "^(?i)r6s\\s(.*)\$"
    const val BROADCAST = "^通知\\s([\\s\\S]+)\$"
    const val ROULETTE = "^切换轮盘模式\$|^开始轮盘(\\s[1-6])?\$|^开枪\$"
    const val MANAGER = "^(?i)(.*)\\s(.*)\\s(.*)\\s([0-9]+)\$"
    const val WORD_CLOUD = "^(我的|本群)(今日|本周|本月|本年)词云\$"
    const val DRIFT_BOTTLE = "^[丢扔]漂流瓶\\s?([\\s\\S]+)\$|^[捡捞]漂流瓶\$|^跳海\$|^查漂流瓶\\s?(.*)\$"
    const val WORD_CLOUD_CRON = "^词云\\s(day|week|month)\$"
    const val NSFW = "^(?i)nsfw标签\\s?[\\s\\S]+\$"
    const val TTS = "^tts\\s([\\s\\S]+)\$"
    const val GROUP_ADD_REQ = "^(同意加群|拒绝加群)\\s(add|invite)\\s([0-9]+)\$"
    const val SEND_LIKE = "^赞我([0-9]+)次\$"
    const val CLEAR_SEND_LIKE = "^重置点赞(.*)\$"

}

