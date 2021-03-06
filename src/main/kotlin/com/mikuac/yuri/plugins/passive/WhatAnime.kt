package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.bean.MsgChainBean
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.bean.dto.WhatAnimeBasicDto
import com.mikuac.yuri.bean.dto.WhatAnimeDto
import com.mikuac.yuri.config.ReadConfig
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
            // ??????????????????
            val basicResult = RequestUtils.get("https://api.trace.moe/search?cutBorders&url=${imgUrl}").body?.string()
            val basicData = Gson().fromJson(basicResult, WhatAnimeBasicDto::class.java)
            if (basicData.error != "") throw YuriException(basicData.error)
            if (basicData.result.isEmpty()) throw YuriException("?????????????????????")
            val animeId = basicData.result[0].aniList
            // ??????????????????
            val variables = JsonObject()
            variables.addProperty("id", animeId)
            val reqBody = JsonObject()
            reqBody.addProperty("query", graphqlQuery)
            reqBody.add("variables", variables)
            val aniListResult = RequestUtils.post("https://trace.moe/anilist/", reqBody.toString()).body?.string()
            val aniListData = Gson().fromJson(aniListResult, WhatAnimeDto::class.java)
            data = Pair(basicData, aniListData)
        } catch (e: Exception) {
            throw YuriException("WhatAnime?????????????????????${e.message}")
        }
        return data
    }

    private fun buildMsg(userId: Long, groupId: Long, arrMsg: List<MsgChainBean>): Pair<String, String>? {
        val imgUrl = SearchModeUtils.getImgUrl(userId, groupId, arrMsg) ?: return null
        // ?????????
        val imgMd5 = imgUrl.split("-").last()
        val cache = repository.findByMd5(imgMd5)
        if (cache.isPresent) {
            return Pair("${cache.get().infoResult}\n[Tips] ???????????????????????????", cache.get().videoResult)
        }

        val result = request(imgUrl)
        val basic = result.first.result[0]
        val detailed = result.second.data.media

        val animeName = detailed.title.chinese.ifEmpty { detailed.title.native }
        val startTime = "${detailed.startDate.year}???${detailed.startDate.month}???${detailed.startDate.day}???"
        val endTime = "${detailed.endDate.year}???${detailed.endDate.month}???${detailed.endDate.day}???"

        val infoMsg = MsgUtils.builder()
            .img(detailed.coverImage.large)
            .text("\n?????????????????????${animeName}???${basic.episode}???")
            .text("\n???????????? ${DateUtils.sToMS(basic.from)} ??? ${DateUtils.sToMS(basic.to)} ??????")
            .text("\n???????????????${detailed.type}-${detailed.format}")
            .text("\n?????????${detailed.status}")
            .text("\n????????????${detailed.episodes}")
            .text("\n???????????????${detailed.season}")
            .text("\n???????????????$startTime")
            .text("\n???????????????$endTime")
            .text("\n???????????????WhatAnime")
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
        // ??????????????????
        try {
            val msg = buildMsg(event.userId, event.groupId, event.arrayMsg) ?: return
            bot.sendMsg(event, msg.first, false)
            // ??????????????????
            if (ReadConfig.config.plugin.whatAnime.sendPreviewVideo) bot.sendMsg(event, msg.second, false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "???????????????${e.message}")
            e.printStackTrace()
        }
    }

}