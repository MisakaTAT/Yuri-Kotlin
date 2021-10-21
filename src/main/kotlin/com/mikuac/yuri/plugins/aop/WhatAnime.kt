package com.mikuac.yuri.plugins.aop

import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.*
import com.mikuac.yuri.dto.WhatAnimeBasicDto
import com.mikuac.yuri.dto.WhatAnimeDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class WhatAnime : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

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
        if (json.error != "") throw Exception(json.error) // TODO: 测试
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

    private fun handleMsg(msg: String): String {
        return RegexUtils.find(
            Regex("(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?"),
            msg
        ) ?: throw Exception("获取图片链接失败")
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!checkUtils.basicCheck(this.javaClass.simpleName, userId, groupId, bot)) return
        try {
            val basic = getBasicInfo(handleMsg(msg)).result[0]
            val detailed = doSearch(basic.anilist).data.media
            var animeName = detailed.title.chinese
            if ("" == animeName) {
                animeName = detailed.title.native
            }
            val startTime = "${detailed.startDate.year}年${detailed.startDate.month}月${detailed.startDate.day}日"
            val endTime = "${detailed.endDate.year}年${detailed.endDate.month}月${detailed.endDate.day}日"
            val msgUtils = MsgUtils.builder()
                .img(detailed.coverImage.large)
                .text("\n该截图出自番剧${animeName}第${basic.episode}集")
                .text("\n截图位于 ${TimeUtils.sToMS(basic.from)} 至 ${TimeUtils.sToMS(basic.to)} 附近")
                .text("\n番剧类型：${detailed.type}-${detailed.format}")
                .text("\n状态：${detailed.status}")
                .text("\n总集数：${detailed.episodes}")
                .text("\n开播季节：${detailed.season}")
                .text("\n开播时间：$startTime")
                .text("\n完结时间：$endTime")
                .text("\n数据来源：WhatAnime")
                .build()
            MsgSendUtils.send(userId, groupId, bot, msgUtils)
//            MsgSendUtils.send(userId, groupId, bot, MsgUtils.builder().video(basic.video,).build())
        } catch (e: Exception) {
            MsgSendUtils.atSend(userId, groupId, bot, "WhatAnime检索失败 ${e.message}")
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
//        buildMsg(event.message, event.userId, event.userId, bot)
        return MESSAGE_IGNORE
    }

}