// package com.mikuac.yuri.plugins
//
// import com.mikuac.shiro.core.Bot
// import com.mikuac.shiro.core.BotPlugin
// import com.mikuac.shiro.dto.event.message.GroupMessageEvent
// import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
// import com.mikuac.yuri.utils.CheckUtils
// import com.mikuac.yuri.utils.MsgSendUtils
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.stereotype.Component
//
// @Component
// class BlackListCheck : BotPlugin() {
//
//     @Autowired
//     private lateinit var checkUtils: CheckUtils
//
//     override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
//         val userId = event.userId
//         if (checkUtils.checkUserInBlackList(userId)) {
//             MsgSendUtils.atSend(userId, 0L, bot, "你被关小黑屋啦，联系管理员试试吧～")
//             return MESSAGE_BLOCK
//         }
//         return MESSAGE_IGNORE
//     }
//
//     override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
//         val userId = event.userId
//         val groupId = event.groupId
//         if (checkUtils.checkGroupInBlackList(groupId)) return MESSAGE_BLOCK
//         if (checkUtils.checkUserInBlackList(userId)) {
//             MsgSendUtils.atSend(userId, groupId, bot, "你被关小黑屋啦，联系管理员试试吧～")
//             return MESSAGE_BLOCK
//         }
//         return MESSAGE_IGNORE
//     }
//
// }