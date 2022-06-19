package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.bean.MsgChainBean
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.WhatAnimeBasicDto
import com.mikuac.yuri.dto.WhatAnimeDto
import com.mikuac.yuri.entity.WhatAnimeCacheEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.WhatAnimeCacheRepository
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RequestUtils
import com.mikuac.yuri.utils.SearchModeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Shiro
@Component
class WhatAnime {

    @Autowired
    private lateinit var repository: WhatAnimeCacheRepository

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

    @Synchronized
    private fun request(imgUrl: String): Pair<WhatAnimeBasicDto, WhatAnimeDto> {
        val data: Pair<WhatAnimeBasicDto, WhatAnimeDto>
        try {
            // 获取基本信息
            val basicResult = RequestUtils.get("https://api.trace.moe/search?cutBorders&url=${imgUrl}").body?.string()
            val basicData = Gson().fromJson(basicResult, WhatAnimeBasicDto::class.java)
            if (basicData.error != "") throw YuriException(basicData.error)
            if (basicData.result.isEmpty()) throw YuriException("未找到匹配结果")
            val animeId = basicData.result[0].aniList
            // 获取详细信息
            val variables = JsonObject()
            variables.addProperty("id", animeId)
            val reqBody = JsonObject()
            reqBody.addProperty("query", graphqlQuery)
            reqBody.add("variables", variables)
            val aniListResult = RequestUtils.post("https://trace.moe/anilist/", reqBody.toString()).body?.string()
            val aniListData = Gson().fromJson(aniListResult, WhatAnimeDto::class.java)
            data = Pair(basicData, aniListData)
        } catch (e: Exception) {
            throw YuriException("WhatAnime数据获取异常：${e.message}")
        }
        return data
    }

    private fun buildMsg(userId: Long, groupId: Long, arrMsg: List<MsgChainBean>): Pair<String, String>? {
        val imgUrl = SearchModeUtils.getImgUrl(userId, groupId, arrMsg) ?: return null
        // 查缓存
        val imgMd5 = imgUrl.split("-").last()
        val cache = repository.findByMd5(imgMd5)
        if (cache.isPresent) {
            return Pair("${cache.get().infoResult}\n[Tips] 该结果为数据库缓存", cache.get().videoResult)
        }

        val result = request(imgUrl)
        val basic = result.first.result[0]
        val detailed = result.second.data.media

        val animeName = detailed.title.chinese.ifEmpty { detailed.title.native }
        val startTime = "${detailed.startDate.year}年${detailed.startDate.month}月${detailed.startDate.day}日"
        val endTime = "${detailed.endDate.year}年${detailed.endDate.month}月${detailed.endDate.day}日"

        val infoMsg = MsgUtils.builder()
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
        val videoMsg = MsgUtils.builder().video(basic.video, imgUrl).build()
        repository.save(WhatAnimeCacheEntity(0, imgMd5, infoMsg, videoMsg))
        return Pair(infoMsg, videoMsg)
    }

    @MessageHandler(cmd = RegexCMD.WHAT_ANIME_SEARCH)
    fun whatAnimeHandler(bot: Bot, event: WholeMessageEvent) {
        SearchModeUtils.setSearchMode(this.javaClass.simpleName, event.userId, event.groupId, bot)
    }

    @MessageHandler
    fun whatAnimeSearch(bot: Bot, event: WholeMessageEvent) {
        if (!SearchModeUtils.check(this.javaClass.simpleName, event.userId, event.groupId)) return
        // 发送检索结果
        try {
            val msg = buildMsg(event.userId, event.groupId, event.arrayMsg) ?: return
            bot.sendMsg(event, msg.first, false)
            // 发送预览视频
            if (ReadConfig.config.plugin.whatAnime.sendPreviewVideo) bot.sendMsg(event, msg.second, false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}