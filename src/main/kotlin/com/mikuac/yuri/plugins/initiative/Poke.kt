package com.mikuac.yuri.plugins.initiative

import com.mikuac.shiro.annotation.GroupPokeNoticeHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent
import com.mikuac.yuri.config.Config
import org.springframework.stereotype.Component

@Shiro
@Component
class Poke {

    private val cfg = Config.base

    @GroupPokeNoticeHandler
    fun handler(bot: Bot, event: PokeNoticeEvent) {
        val groupId = event.groupId
        val userId = event.userId
        val targetId = event.targetId
        if (event.senderId != cfg.selfId && (cfg.selfId == targetId || cfg.adminList.contains(targetId))) {
            bot.sendGroupMsg(groupId, MsgUtils.builder().poke(userId).build(), false)
        }
    }

}
