package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.BangumiDto
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Slf4j
@Component
class Bangumi : BotPlugin() {

    val cache = HashMap<String, ArrayList<String>>()

    @Scheduled(cron = "0 1 1 * * ?")
    fun cleanCache() {
        cache.clear()
        log.info("[${this.javaClass.simpleName}] 缓存已清除")
    }

    private fun request(): BangumiDto? {
        val result = RequestUtils.get("https://api.bgm.tv/calendar") ?: throw YuriException("番组计划API请求失败")
        return Gson().fromJson(result.string(), BangumiDto::class.java)
    }

    private fun buildMsg(): ArrayList<String> {
        val weekday = SimpleDateFormat("EEEE").format(Date())
        val todayCache: ArrayList<String> = cache[weekday] ?: arrayListOf()
        if (todayCache.isNotEmpty()) return todayCache
        val data = request() ?: throw YuriException("番剧数据获取失败")
        val todayAnime = data.filter { weekday == it.weekday.cn }
        val msgList = ArrayList<String>()
        todayAnime.forEach { i ->
            i.items.forEach { j ->
                val msg = MsgUtils.builder()
                msg.text(j.name_cn.ifEmpty { j.name })
                if (j.images != null) {
                    msg.text("\n")
                    msg.img(j.images.large)
                }
                msgList.add(msg.build())
            }
        }
        cache[weekday] = msgList
        return msgList
    }

    @GroupMessageHandler(cmd = RegexCMD.BANGUMI)
    fun handlerBangumi(bot: Bot, event: GroupMessageEvent) {
        try {
            val messages = ShiroUtils.generateForwardMsg(event.selfId, ReadConfig.config.base.botName, buildMsg())
            bot.sendGroupForwardMsg(event.groupId, messages)
        } catch (e: YuriException) {
            bot.sendGroupMsg(event.groupId, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}