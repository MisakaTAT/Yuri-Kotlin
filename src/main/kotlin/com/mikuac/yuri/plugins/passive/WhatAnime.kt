package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.bo.ArrayMsg
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.WhatAnimeBasicDTO
import com.mikuac.yuri.dto.WhatAnimeDTO
import com.mikuac.yuri.entity.WhatAnimeCacheEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.WhatAnimeCacheRepository
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.SearchModeUtils
import com.mikuac.yuri.utils.SendUtils
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
    private fun request(imgUrl: String): Pair<WhatAnimeBasicDTO, WhatAnimeDTO> {
        val data: Pair<WhatAnimeBasicDTO, WhatAnimeDTO>
        try {
            // 获取基本信息
            val basicResult = NetUtils.get(
                "https://api.trace.moe/search?cutBorders&url=${imgUrl}",
                Config.plugins.picSearch.proxy
            )
            val basicData = Gson().fromJson(basicResult.body?.string(), WhatAnimeBasicDTO::class.java)
            basicResult.close()
            if (basicData.error != "") throw YuriException(basicData.error)
            if (basicData.result.isEmpty()) throw YuriException("未找到匹配结果")
            val animeId = basicData.result[0].aniList
            // 获取详细信息
            val variables = JsonObject()
            variables.addProperty("id", animeId)
            val reqBody = JsonObject()
            reqBody.addProperty("query", graphqlQuery)
            reqBody.add("variables", variables)
            val aniListResult = NetUtils.post(
                "https://trace.moe/anilist/",
                reqBody.toString(),
                Config.plugins.picSearch.proxy
            )
            val aniListData = Gson().fromJson(aniListResult.body?.string(), WhatAnimeDTO::class.java)
            aniListResult.close()
            data = Pair(basicData, aniListData)
        } catch (e: Exception) {
            throw YuriException("WhatAnime数据获取异常：${e.message}")
        }
        return data
    }

    private fun buildMsg(userId: Long, groupId: Long, arrMsg: List<ArrayMsg>): Pair<String, String>? {
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

    @AnyMessageHandler(cmd = RegexCMD.WHAT_ANIME_SEARCH)
    fun whatAnimeHandler(bot: Bot, event: AnyMessageEvent) {
        SearchModeUtils.setSearchMode(this.javaClass.simpleName, event.userId, event.groupId ?: 0L, bot)
    }

    @AnyMessageHandler
    fun whatAnimeSearch(bot: Bot, event: AnyMessageEvent) {
        if (!SearchModeUtils.check(this.javaClass.simpleName, event.userId, event.groupId ?: 0L)) return
        // 发送检索结果
        try {
            val msg = buildMsg(event.userId, event.groupId, event.arrayMsg) ?: return
            bot.sendMsg(event, msg.first, false)
            // 发送预览视频
            if (Config.plugins.picSearch.animePreviewVideo) bot.sendMsg(event, msg.second, false)
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}