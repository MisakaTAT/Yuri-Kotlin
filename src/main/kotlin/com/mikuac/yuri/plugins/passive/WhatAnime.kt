package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.bean.MsgChainBean
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.WhatAnimeBasicDto
import com.mikuac.yuri.dto.WhatAnimeDto
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.RequestUtils
import com.mikuac.yuri.utils.SearchModeUtils
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
        if (json.result.isEmpty()) throw Exception("未找到匹配结果")
        return json
    }

    private fun doSearch(animeId: Long): WhatAnimeDto {
        val variables = JsonObject()
        variables.addProperty("id", animeId)
        val json = JsonObject()
        json.addProperty("query", graphqlQuery)
        json.add("variables", variables)
        val result = RequestUtils.post("https://trace.moe/anilist/", json.toString())
        return Gson().fromJson(result, WhatAnimeDto::class.java)
    }

    private fun buildMsg(userId: Long, groupId: Long, arrMsg: List<MsgChainBean>): Pair<String, String>? {
        // 重新设置过期时间
        SearchModeUtils.resetExpiration(userId, groupId)

        val images = arrMsg.filter { "image" == it.type }
        if (images.isEmpty()) return null
        val imgUrl = images[0].data["url"] ?: return null

        val basic = getBasicInfo(imgUrl).result[0]
        val detailed = doSearch(basic.aniList).data.media
        val animeName = detailed.title.chinese.ifEmpty { detailed.title.native }
        val startTime = "${detailed.startDate.year}年${detailed.startDate.month}月${detailed.startDate.day}日"
        val endTime = "${detailed.endDate.year}年${detailed.endDate.month}月${detailed.endDate.day}日"

        return Pair(
            MsgUtils.builder()
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
                .build(), MsgUtils.builder().video(basic.video, imgUrl).build()
        )
    }

    @MessageHandler(cmd = RegexCMD.WHAT_ANIME_SEARCH)
    fun handler(bot: Bot, event: WholeMessageEvent) {
        SearchModeUtils.setSearchMode(this.javaClass.simpleName, event.userId, event.groupId, bot)
    }

    @MessageHandler
    fun search(bot: Bot, event: WholeMessageEvent) {
        if (!SearchModeUtils.check(this.javaClass.simpleName, event.userId, event.groupId)) return
        // 发送检索结果
        try {
            val msg = buildMsg(event.userId, event.groupId, event.arrayMsg) ?: return
            bot.sendMsg(event, msg.first, false)
            // 发送预览视频
            if (ReadConfig.config.plugin.whatAnime.sendPreviewVideo) bot.sendMsg(event, msg.second, false)
        } catch (e: YuriException) {
            bot.sendMsg(event, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}