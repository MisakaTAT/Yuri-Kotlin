package com.mikuac.yuri.plugins

import cn.hutool.cache.CacheUtil
import cn.hutool.cache.impl.TimedCache
import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.task.AsyncTask
import com.mikuac.yuri.common.utils.RequestUtils
import com.mikuac.yuri.dto.SexPicDto
import org.springframework.stereotype.Component
import javax.annotation.Resource

@Component
class SexPic : BotPlugin() {

    @Resource
    private lateinit var asyncTask: AsyncTask

    private val cdTime = ReadConfig.config?.plugin?.sexPic?.cdTime

    private val timedCache: TimedCache<Long, Long> = CacheUtil.newTimedCache(cdTime?.times(1000L) ?: 60000)

    private fun request(): SexPicDto.Data? {
        val result = ReadConfig.config?.plugin?.sexPic?.api?.let { RequestUtils.get(it) } ?: return null
        val json = Gson().fromJson(result, SexPicDto::class.java) ?: return null
        return json.data[0]
    }

    /**
     * Triple<Boolean, String, String?>
     * 第一个参数为是否请求成功
     * 第二个参数为响应的文本内容
     * 第三个参数为图片链接（理论上请求成功就不会为null）
     */
    private fun buildTextMsg(): Triple<Boolean, String, String?> {
        val data = request() ?: return Triple(false, "诶呀...，请求好像出现了一些问题，要不重新试试？", null)
        return Triple(
            true,
            MsgUtils.builder()
                .text("标题：${data.title}")
                .text("\nPID：${data.pid}")
                .text("\n作者：${data.author}")
                .text("\n链接：https://www.pixiv.net/artworks/${data.pid}")
                .text("\n反代链接：${data.urls.original}")
                .build(), data.urls.original
        )
    }

    private fun buildPicMsg(url: String?): String {
        url ?: return "图片获取失败了呢 _(:3 」∠)_"
        return MsgUtils.builder().img(url).build()
    }

    private fun check(groupId: Long, userId: Long, bot: Bot): Boolean {
        // 检查是否处于冷却时间
        if (timedCache.get(groupId) != null && timedCache.get(groupId) == userId) {
            bot.sendGroupMsg(groupId, MsgUtils.builder().at(userId).text("整天色图色图，信不信把你变成色图？").build(), false)
            return false
        }
        return true
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.equals("test")) {
            val groupId = event.groupId
            val userId = event.userId
            // 一些前置检查
            if (!check(groupId, userId, bot)) return MESSAGE_IGNORE
            bot.sendGroupMsg(groupId, buildTextMsg().second, false)
            if (buildTextMsg().first) {
                // 如果 buildTextMsg().first 为 true 则认为请求成功，这时候将请求者信息放入 Map
                timedCache.put(groupId, userId)
                val msgId = bot.sendGroupMsg(groupId, buildPicMsg(buildTextMsg().third), false).data.messageId
                asyncTask.deleteMsg(msgId, bot)
            }
        }
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        return MESSAGE_IGNORE
    }

}