package com.mikuac.yuri.plugins

import com.mikuac.shiro.common.utils.ShiroUtils
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

    private fun check(msgId: Int, msg: String, selfId: Long, userId: Long, groupId: Long, bot: Bot) {
        val atList = ShiroUtils.getAtList(msg)
        if (!atList.contains(selfId.toString()) || atList.size > 1) return
        val handleMsg = msg.replace("^\\[CQ.*?]".toRegex(), "").trim()
        if (handleMsg.isEmpty()) return
        buildMsg(msgId, handleMsg, userId, groupId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(event.messageId, event.message, event.selfId, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}