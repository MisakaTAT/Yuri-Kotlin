package com.mikuac.yuri.aop

import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.SearchModeUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class CommandPrefixAspect {

    @Around(value = "execution(int com.mikuac.yuri.plugins.aop.*.on*Message(..))")
    private fun prefixCheck(pjp: ProceedingJoinPoint): Int {
        val prefix = ReadConfig.config.command.prefix
        val args = pjp.args
        args.forEachIndexed { index, arg ->
            if (arg is GroupMessageEvent) {
                val msg = arg.message
                // 如果消息未携带prefix，且未匹配到纯图片信息则拦截
                if (!msg.startsWith(prefix) && !msg.matches(Regex("^\\[CQ:image(.*?)]"))) return BotPlugin.MESSAGE_IGNORE
                // 匹配到纯图片信息判断用户是否处于搜图模式，否则拦截
                if (msg.matches(Regex("^\\[CQ:image(.*?)]"))) {
                    SearchModeUtils.expiringMap[arg.userId + arg.groupId] ?: return BotPlugin.MESSAGE_IGNORE
                }
                // 去除消息prefix并放行
                if (msg.startsWith(prefix)) {
                    val msgBuilder = arg.toBuilder()
                    msgBuilder.message(msg.substring(prefix.length))
                    args[index] = msgBuilder.build()
                }
            }
            if (arg is PrivateMessageEvent) {
                val msg = arg.message
                // 如果消息未携带prefix，且未匹配到纯图片信息则拦截
                if (!msg.startsWith(prefix) && !msg.matches(Regex("^\\[CQ:image(.*?)]"))) return BotPlugin.MESSAGE_IGNORE
                // 匹配到纯图片信息判断用户是否处于搜图模式，否则拦截
                if (msg.matches(Regex("^\\[CQ:image(.*?)]"))) {
                    SearchModeUtils.expiringMap[arg.userId] ?: return BotPlugin.MESSAGE_IGNORE
                }
                // 去除消息prefix并放行
                if (msg.startsWith(prefix)) {
                    val msgBuilder = arg.toBuilder()
                    msgBuilder.message(msg.substring(prefix.length))
                    args[index] = msgBuilder.build()
                }
            }
        }
        return pjp.proceed(args) as Int
    }

}