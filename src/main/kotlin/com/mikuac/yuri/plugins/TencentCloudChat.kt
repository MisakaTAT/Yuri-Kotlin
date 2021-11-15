package com.mikuac.yuri.plugins

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.TencentUtils
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class TencentCloudChat : BotPlugin() {

    private val log = KotlinLogging.logger {}

    private fun buildMsg(msgId: Int, query: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            val tbpConfig = ReadConfig.config.plugin.tencentTBP
            val nlpConfig = ReadConfig.config.plugin.tencentNLP
            if (!tbpConfig.enable && !nlpConfig.enable) return
            if (tbpConfig.enable && nlpConfig.enable) {
                log.error { "TencentNLP and TencentTBP not allowed all enable" }
                throw Exception("当前配置文件有误")
            }
            var reply = "请求失败了呢，你这话题人家没法回答QAQ"
            if (tbpConfig.enable) {
                reply = TencentUtils.tbp(query)
            }
            if (nlpConfig.enable) {
                reply = TencentUtils.nlp(query)
            }
            MsgSendUtils.replySend(msgId, userId, groupId, bot, reply)
        } catch (e: Exception) {
            MsgSendUtils.atSend(userId, groupId, bot, "智能对话异常 ${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    private fun check(bot: Bot, event: GroupMessageEvent) {
        event.arrayMsg.filter {
            it.type == "at" && it.data["qq"] == event.selfId.toString()
        }[0].data["qq"] ?: return
        val msg = event.arrayMsg.filter { it.type == "text" }[0].data["text"]?.trim() ?: return
        if (msg.isEmpty()) return
        buildMsg(event.messageId, msg, event.userId, event.groupId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(bot, event)
        return MESSAGE_IGNORE
    }

}
