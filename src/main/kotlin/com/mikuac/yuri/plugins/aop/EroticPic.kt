package com.mikuac.yuri.plugins.aop

import cn.hutool.cache.CacheUtil
import cn.hutool.cache.impl.TimedCache
import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.LogUtils
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.common.utils.RequestUtils
import com.mikuac.yuri.dto.EroticPicDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EroticPic : BotPlugin() {

    private val regex =
        Regex("^[来來发發给給]([1一])?[张張个個幅点點份]([Rr]18的?)?[色瑟][图圖]|^setu(\\s[Rr]18)?|^[色瑟][图圖](\\s[Rr]18)?")

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private val cdTime = ReadConfig.config.plugin.eroticPic.cdTime

    private val timedCache: TimedCache<Long, Long> = CacheUtil.newTimedCache(cdTime.times(1000L))

    private fun request(r18: Boolean): EroticPicDto.Data? {
        var api = ReadConfig.config.plugin.eroticPic.api
        if (r18) api = ReadConfig.config.plugin.eroticPic.api + "?r18=1"
        val result = RequestUtils.get(api) ?: return null
        LogUtils.debug("Erotic pic request result - $result")
        val json = Gson().fromJson(result, EroticPicDto::class.java) ?: return null
        return json.data[0]
    }

    /**
     * Triple<Boolean, String, String?>
     * 第一个参数为是否请求成功
     * 第二个参数为响应的文本内容
     * 第三个参数为图片链接（理论上请求成功就不会为null）
     */
    private fun buildTextMsg(r18: Boolean): Triple<Boolean, String, String?> {
        val data = request(r18) ?: return Triple(false, "诶呀...，请求好像出现了一些问题，要不重新试试？", null)
        return Triple(
            true,
            MsgUtils.builder()
                .text("标题：${data.title}")
                .text("\nPID：${data.pid}")
                .text("\n作者：${data.author}")
                .text("\n链接：https://www.pixiv.net/artworks/${data.pid}")
                .text("\n反代链接：${data.urls.original}")
                .build(),
            data.urls.original
        )
    }

    private fun buildPicMsg(url: String?): String {
        url ?: return "图片获取失败了呢 _(:3 」∠)_"
        return MsgUtils.builder().img(url).build()
    }

    private fun check(groupId: Long, userId: Long, bot: Bot): Boolean {
        if (checkUtils.pluginIsDisable(this.javaClass.simpleName, userId, groupId, bot)) return false
        // 检查是否处于冷却时间
        if (timedCache.get(groupId + userId) != null && timedCache.get(groupId + userId) == userId) {
            MsgSendUtils.sendAll(userId, groupId, bot, "整天色图色图，信不信把你变成色图？")
            return false
        }
        if (checkUtils.checkUserInBlackList(userId, groupId, bot)) return false
        return true
    }

    private fun recallMsgPic(msgId: Int, bot: Bot) = runBlocking {
        launch {
            delay(ReadConfig.config.plugin.eroticPic.recallMsgPicTime.times(1000L))
            bot.deleteMsg(msgId)
            log.info { "Recall erotic pic msg img - MsgID: $msgId" }
        }
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            val groupId = event.groupId
            val userId = event.userId
            val r18 = msg.contains(Regex("(?i)r18"))
            // 一些前置检查
            if (!check(groupId, userId, bot)) return MESSAGE_IGNORE
            val buildTextMsg = buildTextMsg(r18)
            bot.sendGroupMsg(groupId, buildTextMsg.second, false)
            if (buildTextMsg.first) {
                // 如果 buildTextMsg.first 为 true 则认为请求成功，这时候将请求者信息放入 Map
                timedCache.put(groupId + userId, userId)
                val msgId = bot.sendGroupMsg(groupId, buildPicMsg(buildTextMsg.third), false).data.messageId
                recallMsgPic(msgId, bot)
                log.info { "Erotic pic group - Group: $groupId User: $userId" }
            }
        }
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            val userId = event.userId
            val r18 = msg.contains(Regex("(?i)r18"))
            // 一些前置检查
            if (!check(0L, userId, bot)) return MESSAGE_IGNORE
            val buildTextMsg = buildTextMsg(r18)
            bot.sendPrivateMsg(userId, buildTextMsg.second, false)
            if (buildTextMsg.first) {
                // 如果 buildTextMsg.first 为 true 则认为请求成功，这时候将请求者信息放入 Map
                timedCache.put(userId, userId)
                val msgId = bot.sendPrivateMsg(userId, buildPicMsg(buildTextMsg.third), false).data.messageId
                recallMsgPic(msgId, bot)
                log.info { "Erotic pic private - User: $userId" }
            }
        }
        return MESSAGE_IGNORE
    }

}