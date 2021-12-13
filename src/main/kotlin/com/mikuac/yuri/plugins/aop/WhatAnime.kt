package com.mikuac.yuri.plugins.aop

import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.WhatAnimeBasicDto
import com.mikuac.yuri.dto.WhatAnimeDto
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.*
import org.springframework.stereotype.Component

@Component
class WhatAnime : BotPlugin() {

    private val graphqlQuery = """
           query (${'$'}id: Int) {
                  Media (id: ${'$'}id, type: ANIME) {
                    id
                    type
                    format
                    status
                    episodes
                    season
                    synonyms
                    title {
                      native
                      romaji
                      english
                    }
                    startDate {
                      year
                      month
                      day
                    }
                    endDate {
                      year
                      month
                      day
                    }
                    coverImage {
                      large
                    }
                  }
                }
    """

    private fun getBasicInfo(imgUrl: String): WhatAnimeBasicDto {
        val result = RequestUtils.get("https://api.trace.moe/search?cutBorders&url=${imgUrl}")
        val json = Gson().fromJson(result, WhatAnimeBasicDto::class.java)
        if (json.error != "") throw Exception(json.error)
        if (json.result.isEmpty()) throw Exception("未找到匹配内容")
        return json
    }

    private fun doSearch(animeId: Long): WhatAnimeDto {
        val variables = JSONObject()
        variables["id"] = animeId
        val json = JSONObject()
        json["query"] = graphqlQuery
        json["variables"] = variables
        val result = RequestUtils.post("https://trace.moe/anilist/", json.toJSONString())
        return Gson().fromJson(result, WhatAnimeDto::class.java)
    }

    private fun handler(mode: String, msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!SearchModeUtils.check(
                RegexEnum.WHAT_ANIME_SET.value,
                RegexEnum.WHAT_ANIME_UNSET.value,
                msg,
                mode,
                userId,
                groupId,
                bot
            )
        ) return
        buildMsg(msg, userId, groupId, bot)
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            // 重新设置过期时间
            SearchModeUtils.resetExpiration(userId, groupId)
            val imgUrl = RegexUtils.group(Regex("^\\[CQ:image(.*)url=(.*)]"), 2, msg)
            val basic = getBasicInfo(imgUrl).result[0]
            val detailed = doSearch(basic.aniList).data.media
            val animeName = detailed.title.chinese.ifEmpty { detailed.title.native }
            val startTime = "${detailed.startDate.year}年${detailed.startDate.month}月${detailed.startDate.day}日"
            val endTime = "${detailed.endDate.year}年${detailed.endDate.month}月${detailed.endDate.day}日"
            val msgUtils = MsgUtils.builder()
                .img(detailed.coverImage.large)
                .text("\n该截图出自番剧${animeName}第${basic.episode}集")
                .text("\n截图位于 ${DateUtils.sToMS(basic.from)} 至 ${DateUtils.sToMS(basic.to)} 附近")
                .text("\n番剧类型：${detailed.type}-${detailed.format}")
                .text("\n状态：${detailed.status}")
                .text("\n总集数：${detailed.episodes}")
                .text("\n开播季节：${detailed.season}")
                .text("\n开播时间：$startTime")
                .text("\n完结时间：$endTime")
                .text("\n数据来源：WhatAnime")
                .build()
            MsgSendUtils.send(userId, groupId, bot, msgUtils)
            // 发送预览视频
            val sendVideo = ReadConfig.config.plugin.whatAnime.sendPreviewVideo
            if (sendVideo) MsgSendUtils.send(
                userId,
                groupId,
                bot,
                MsgUtils.builder().video(basic.video, imgUrl).build()
            )
        } catch (e: Exception) {
            MsgSendUtils.atSend(userId, groupId, bot, "WhatAnime检索失败：${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        handler(this.javaClass.simpleName, event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(this.javaClass.simpleName, event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}