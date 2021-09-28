package com.mikuac.yuri.aop

import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Aspect
@Component
class CommandPrefixAspect {

    @Pointcut(value = "execution(int com.mikuac.yuri.plugins.aop.*.on*Message(..))")
    private fun point() {
    }

    @Around(value = "point()")
    private fun prefixCheck(pjp: ProceedingJoinPoint): Int {
        val prefix = ReadConfig.config?.command?.prefix ?: ""
        val args = pjp.args
        args.forEachIndexed { index, arg ->
            if (arg is GroupMessageEvent) {
                val msg = arg.message
                if (!msg.startsWith(prefix)) return BotPlugin.MESSAGE_IGNORE
                val msgBuilder = arg.toBuilder()
                msgBuilder.message(msg.substring(prefix.length))
                args[index] = msgBuilder.build()
            }
            if (arg is PrivateMessageEvent) {
                val msg = arg.message
                if (!msg.startsWith(prefix)) return BotPlugin.MESSAGE_IGNORE
                val msgBuilder = arg.toBuilder()
                msgBuilder.message(msg.substring(prefix.length))
                args[index] = msgBuilder.build()
            }
        }
        return pjp.proceed(args) as Int
    }

}