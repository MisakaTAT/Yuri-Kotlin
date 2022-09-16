package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.request.GroupAddRequestEvent
import com.mikuac.yuri.config.Config
import org.springframework.stereotype.Component

@Component
class GroupInvite : BotPlugin() {

    override fun onGroupAddRequest(bot: Bot, event: GroupAddRequestEvent): Int {
        Config.base.adminList.forEach {
            val msg = MsgUtils.builder()
                .text("邀请人: ${event.userId}")
                .text("\n目标群: ${event.groupId}")
                .text("\n备注: ${event.comment}")
                .text("\n标识: ${event.flag}")
                .build()
            bot.sendPrivateMsg(it, msg, false)
        }
        return MESSAGE_IGNORE
    }

}