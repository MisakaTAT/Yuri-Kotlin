package com.mikuac.yuri.plugins

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.LogUtils
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
        val userInfo = bot.getGroupMemberInfo(groupId, userId, true).data
        val targetInfo = bot.getGroupMemberInfo(groupId, targetId, true).data
        if (userInfo != null && targetInfo != null) {
            LogUtils.debug("Poke事件（群：${groupId} 用户：${userInfo.nickname} 目标：${targetInfo.nickname}）")
            return MESSAGE_IGNORE
        }
        LogUtils.debug("Poke事件（群：${groupId} 用户：${userId} 目标：${targetId}）")
        return MESSAGE_IGNORE
    }

}




