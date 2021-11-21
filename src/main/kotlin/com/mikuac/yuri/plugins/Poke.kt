package com.mikuac.yuri.plugins

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent
import com.mikuac.yuri.config.ReadConfig
import org.springframework.stereotype.Component

@Component
class Poke : BotPlugin() {

    override fun onGroupPokeNotice(bot: Bot, event: PokeNoticeEvent): Int {
        val baseConfig = ReadConfig.config.base
        val groupId = event.groupId
        val userId = event.userId
        val targetId = event.targetId
        if (event.senderId != baseConfig.botSelfId) {
            if (baseConfig.botSelfId == targetId || baseConfig.adminList.contains(targetId)) {
                bot.sendGroupMsg(groupId, MsgUtils.builder().poke(userId).build(), false)
            }
        }
        return MESSAGE_IGNORE
    }

}




