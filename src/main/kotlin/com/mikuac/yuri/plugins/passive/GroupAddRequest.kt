package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.PrivateMessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.shiro.dto.event.request.GroupAddRequestEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Component
class GroupAddRequest : BotPlugin() {

    override fun onGroupAddRequest(bot: Bot, event: GroupAddRequestEvent): Int {
        Config.base.adminList.forEach {
            val msg = MsgUtils.builder()
                .text("申请人: ${event.userId}")
                .text("\n目标群: ${event.groupId}")
                .text("\n备注: ${event.comment}")
                .text("\n标识: ${event.flag}")
                .text("\n加群类型: ${event.subType}")
                .build()
            bot.sendPrivateMsg(it, msg, false)
        }
        return MESSAGE_IGNORE
    }

    @PrivateMessageHandler(cmd = RegexCMD.GROUP_ADD_REQ)
    fun groupAddHandler(event: PrivateMessageEvent, bot: Bot, matcher: Matcher) {
        val type = matcher.group(1)
        val reqType = matcher.group(2)
        val flag = matcher.group(3)
        when (type) {
            "同意加群" -> bot.setGroupAddRequest(flag, reqType, true, "")
            "拒绝加群" -> bot.setGroupAddRequest(flag, reqType, false, "请求被拒")
        }
    }

}