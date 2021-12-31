// package com.mikuac.yuri.aop
//
// import com.mikuac.shiro.core.BotPlugin
// import com.mikuac.shiro.dto.event.message.GroupMessageEvent
// import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
// import com.mikuac.yuri.config.ReadConfig
// import com.mikuac.yuri.utils.SearchModeUtils
// import org.aspectj.lang.ProceedingJoinPoint
// import org.aspectj.lang.annotation.Around
// import org.aspectj.lang.annotation.Aspect
// import org.springframework.stereotype.Component
//
// @Aspect
// @Component
// class CommandPrefixAspect {
//
//     @Around(value = "execution(int com.mikuac.yuri.plugins.aop.*.on*Message(..))")
//     private fun prefixCheck(pjp: ProceedingJoinPoint): Int {
//         val prefix = ReadConfig.config.command.prefix
//         val args = pjp.args
//         args.forEachIndexed { index, arg ->
//             if (arg is GroupMessageEvent) {
//                 val msg = arg.message
//                 // 如果消息未携带prefix，且未匹配到纯图片信息则拦截
//                 val imgList = arg.arrayMsg.filter { "image" == it.type }
//                 if (!msg.startsWith(prefix) && imgList.isEmpty()) return BotPlugin.MESSAGE_IGNORE
//                 // 匹配到纯图片信息判断用户是否处于搜图模式，否则拦截
//                 if (imgList.size == 1) {
//                     SearchModeUtils.expiringMap[arg.userId + arg.groupId] ?: return BotPlugin.MESSAGE_IGNORE
//                 }
//                 // 去除消息prefix并放行
//                 if (msg.startsWith(prefix)) {
//                     val msgBuilder = arg.toBuilder()
//                     msgBuilder.message(msg.substring(prefix.length))
//                     args[index] = msgBuilder.build()
//                 }
//             }
//             if (arg is PrivateMessageEvent) {
//                 val msg = arg.message
//                 // 如果消息未携带prefix，且未匹配到纯图片信息则拦截
//                 val imgList = arg.arrayMsg.filter { "image" == it.type }
//                 if (!msg.startsWith(prefix) && imgList.isEmpty()) return BotPlugin.MESSAGE_IGNORE
//                 // 匹配到纯图片信息判断用户是否处于搜图模式，否则拦截
//                 if (imgList.size == 1) {
//                     SearchModeUtils.expiringMap[arg.userId] ?: return BotPlugin.MESSAGE_IGNORE
//                 }
//                 // 去除消息prefix并放行
//                 if (msg.startsWith(prefix)) {
//                     val msgBuilder = arg.toBuilder()
//                     msgBuilder.message(msg.substring(prefix.length))
//                     args[index] = msgBuilder.build()
//                 }
//             }
//         }
//         return pjp.proceed(args) as Int
//     }
//
// }