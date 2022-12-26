package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.SendUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class SendLike {

    companion object {
        private const val ZONE = "Asia/Shanghai"
    }

    private val status: HashMap<Long, Int> = HashMap()

    @Scheduled(cron = "0 5 00 * * ?", zone = ZONE)
    fun taskForDay() {
        status.clear()
    }

    @GroupMessageHandler(cmd = RegexCMD.CLEAR_SEND_LIKE)
    fun clearSendLikeStatus(bot: Bot, event: GroupMessageEvent) {
        if (event.userId !in Config.base.adminList) {
            bot.sendGroupMsg(event.groupId, "此操作需要管理员权限", false)
            return
        }
        event.arrayMsg.filter { "at" == it.type }.forEach {
            status[it.data["qq"]!!.toLong()] = 0
            bot.sendGroupMsg(event.groupId, "重置成功", false)
        }
    }

    @GroupMessageHandler(cmd = RegexCMD.SEND_LIKE)
    fun sendLikeHandler(bot: Bot, event: GroupMessageEvent, matcher: Matcher) {
        try {
            val userId = event.userId
            val maxTimes = Config.plugins.sendLike.maxTimes
            val count = status.getOrDefault(userId, 0)
            val currentTimes = maxTimes - count
            if (currentTimes > maxTimes) throw YuriException("您已达到当日最大点赞次数")
            val times = matcher.group(1).toIntOrNull() ?: throw YuriException("爬")
            if (currentTimes < times) throw YuriException("今日可用点赞数不足、剩余${currentTimes}次")
            if (times <= 0 || times > 20) throw YuriException("点赞次数低于最小值或超过最大值")
            bot.sendLike(event.userId, times)
            bot.sendGroupMsg(event.groupId, MsgUtils.builder().reply(event.messageId).text("点赞完成啦").build(), false)
            status[userId] = count + times
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}