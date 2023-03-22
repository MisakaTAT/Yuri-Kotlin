package com.mikuac.yuri.aop

import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
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

    @Before("execution(* com.mikuac.yuri.plugins.passive.*.handler(..))")
    fun handler(jp: JoinPoint) {
        jp.args.forEach { arg ->
            when (arg) {
                is AnyMessageEvent -> {
                    arg.let {
                        val msgType = it.messageType
                        val handlerName = it.javaClass.simpleName
                        val className = jp.target.javaClass.simpleName
                        val plugin = "[${className}:${handlerName}](${upperCase(msgType)})"
                        val group = if ("group" != it.messageType) "" else "Group(${it.groupId})"
                        val user = "User(${it.userId})"
                        val req = if (group.isNotEmpty()) "$group/$user" else user
                        log.info("$plugin -> $req -> ${it.message}")
                        return
                    }
                }

                is PrivateMessageEvent -> {
                    arg.let {
                        val handlerName = it.javaClass.simpleName
                        val className = jp.target.javaClass.simpleName
                        log.info("[${className}:${handlerName}](Private) -> User(${arg.userId}) -> ${arg.message}")

                    }
                }

                is GroupMessageEvent -> {
                    arg.let {
                        val handlerName = it.javaClass.simpleName
                        val className = jp.target.javaClass.simpleName
                        log.info("[${className}:${handlerName}](Group) -> Group(${it.groupId})/User(${it.userId}) -> ${it.message}")
                    }
                }
            }
        }
    }
}
