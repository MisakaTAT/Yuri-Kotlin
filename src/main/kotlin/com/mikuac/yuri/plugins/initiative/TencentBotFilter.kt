// package com.mikuac.yuri.plugins.initiative
//
// import com.mikuac.shiro.core.Bot
// import com.mikuac.shiro.core.BotPlugin
// import com.mikuac.shiro.dto.event.message.GroupMessageEvent
// import org.springframework.stereotype.Component
//
// @Component
// class TencentBotFilter : BotPlugin() {
//
//     override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
//         val userId = event.userId
//         if (userId in 2854196300L..2854216399L) {
//             return MESSAGE_BLOCK
//         }
//         return MESSAGE_IGNORE
//     }
//
// }