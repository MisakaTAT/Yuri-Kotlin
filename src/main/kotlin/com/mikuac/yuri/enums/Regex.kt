package com.mikuac.yuri.enums

@Suppress("SpellCheckingInspection")
object Regex {

    const val CHAT_GPT = "^(?i)chat\\s(?<action>set|del|show)?\\s?(?<prompt>[\\s\\S]+?)?\$"
    const val DOU_YIN_REAL_URL_ID = "/video/(?<id>\\d+)"
    const val DOU_YIN_SHORT_URL = "(?<url>(?:https?:\\/\\/)?v\\.douyin\\.com\\/\\w+)"
    const val BILIBILI_BID = "(?<bid>BV[A-Za-z0-9]+)"
    const val YOUTUBE_URL = "(?:https?://)?(?:www\\.)?(?:youtube\\.com|youtu\\.be)/watch\\?v=(?<id>[^#&?]*)"
    const val BOT_STATUS = "^(?i)status|状态\$"
    const val THROW_USER = "^(?i)(?:throw|[丢抛]).*"
    const val BV_AV_CONVERT = "^(?i)(?<action>bv[2转]av|av[2转]bv)\\s(?<id>.*)"
    const val HTTP_CAT = "^(?i)httpcat\\s([0-9]+)"
    const val PHOENIX_WRIGHT = "^(?i)(?:逆转裁判|nzcp)\\s+(?<top>.*)\\s+(?<bottom>.*)"
    const val GITHUB_REPO = "^(?i)github\\s(?<repo>.*)"
    const val EPIC_FREE_GAME = "^(?i)epic\$"
    const val SAUCE_NAO_SEARCH = "^[搜识][图圖]\$"
    const val WHAT_ANIME_SEARCH = "^[搜找]番\$"
    const val UNSET_SEARCH_MODE = "^结束检索|谢谢|退出搜图\$"
    const val HELP = "^(?i)help|帮助\$"
    const val TAROT = "^(?i)tarot|塔罗牌\$"
    const val HITOKOTO = "^(?i)hitokoto|一言\$"
    const val SETU = "^(?i)(?:setu|色图)(?<r18>\\sr18)?\$"
    const val WEB_SCREENSHOT = "(?<action>(?:全屏)?网页截图)\\s(?<url>https?://[^\\s]*)\\s*(?<selector>.*)?"
    const val VITS = "^(?i)vits(?<model>\\s[0-9]+)?\\s(?<text>[\\s\\S]+)"
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
    const val STEAM_PLAYER_STATUS = "^steam\\s(?<action>bd|subs|ubd)?\\s?(?<steamId>[0-9]+)?"
    const val TEMP_EMAIL = "^临时邮箱$"
    const val UNSET_TEMP_EMAIL = "^邮箱退出\$"
    const val VOTE_KICK = "^(发起踢人投票.*|赞成$|反对$|结束投票$)"
}

