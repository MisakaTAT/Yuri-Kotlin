package com.mikuac.yuri.aop

import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component

@Slf4j
@Aspect
@Component
class ActionLogAspect {

    private fun upperCase(s: String): String {
        val ch = s.toCharArray()
        if (ch[0] in 'a'..'z') {
            ch[0] = (ch[0] - 32)
        }
        return String(ch)
    }

    @Before("execution(* com.mikuac.yuri.plugins.passive.*.*Handler(..))")
    fun handler(jp: JoinPoint) {
        jp.args.forEach { arg ->
            if (arg is AnyMessageEvent) {
                val msgType = arg.messageType
                val pluginInfo =
                    "[${jp.target.javaClass.simpleName}:${arg.javaClass.simpleName}](${upperCase(msgType)})"
                val groupInfo = if ("group" != arg.messageType) "" else "Group(${arg.groupId})"
                val userInfo = "User(${arg.userId})"
                val reqInfo = if (groupInfo.isNotEmpty()) "$groupInfo/$userInfo" else userInfo
                log.info("$pluginInfo -> $reqInfo -> ${arg.message}")
                return
            }
            if (arg is PrivateMessageEvent) {
                log.info("[${jp.target.javaClass.simpleName}:${arg.javaClass.simpleName}](Private) -> User(${arg.userId}) -> ${arg.message}")
                return
            }
            if (arg is GroupMessageEvent) {
                log.info("[${jp.target.javaClass.simpleName}:${arg.javaClass.simpleName}](Group) -> Group(${arg.groupId})/User(${arg.userId}) -> ${arg.message}")
                return
            }
        }
    }

}
