package com.mikuac.yuri.plugins

import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.exception.TencentCloudSDKException
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.nlp.v20190408.NlpClient
import com.tencentcloudapi.nlp.v20190408.models.ChatBotRequest
import org.springframework.stereotype.Component

@Component
class TencentNLP : BotPlugin() {

    private fun request(query: String): String {
        val config = ReadConfig.config.plugin.tencentNLP
        val cred = Credential(config.secretId, config.secretKey)
        val httpProfile = HttpProfile()
        httpProfile.endpoint = "nlp.tencentcloudapi.com"
        val clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        val client = NlpClient(cred, "ap-guangzhou", clientProfile)
        val req = ChatBotRequest()
        req.query = query
        return client.ChatBot(req).reply
    }

    private fun buildMsg(msgId: Int, query: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            val reply = request(query)
            MsgSendUtils.replySend(msgId, userId, groupId, bot, reply)
        } catch (e: TencentCloudSDKException) {
            MsgSendUtils.atSend(userId, groupId, bot, "TencentNLP请求异常 ${e.message}")
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