package com.mikuac.yuri.plugins.initiative

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.notice.GroupDecreaseNoticeEvent
import com.mikuac.shiro.dto.event.notice.GroupIncreaseNoticeEvent
import com.mikuac.yuri.config.ReadConfig
import org.springframework.stereotype.Component

@Component
class GroupJoinAndQuit : BotPlugin() {

    override fun onGroupDecreaseNotice(bot: Bot, event: GroupDecreaseNoticeEvent): Int {
        if ("kick" == event.subType) {
            val nickname = bot.getGroupMemberInfo(event.groupId, event.operatorId, false).data.nickname
            bot.sendGroupMsg(event.groupId, "${event.userId} 被 $nickname 移出群聊", false)
        }
        if ("leave" == event.subType) {
            bot.sendGroupMsg(event.groupId, "${event.userId} 退出群聊", false)
        }
        return MESSAGE_IGNORE
    }

    override fun onGroupIncreaseNotice(bot: Bot, event: GroupIncreaseNoticeEvent): Int {
        val groupId = event.groupId
        val userId = event.userId
        val botName = ReadConfig.config.base.botName
        val prefix = ReadConfig.config.command.prefix
        // 排除BOT自身入群通知
        if (userId == ReadConfig.config.base.botSelfId) return MESSAGE_IGNORE
        val msg = MsgUtils.builder()
            .at(userId)
            .text("Hi~ 我是${botName}，欢迎加入本群，如果想了解我，请发送 帮助 或 help 获取帮助信息。")
        bot.sendGroupMsg(groupId, msg.build(), false)
        return MESSAGE_IGNORE
    }

}