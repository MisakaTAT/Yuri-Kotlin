package com.mikuac.yuri.aop

import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.utils.CheckUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Aspect
@Component
class ManagerCheckAspect {

    private val cfg = Config.base

    @Autowired
    private lateinit var utils: CheckUtils

    // 过滤腾讯官方机器人
    private fun filterTencentBot(userId: Long): Boolean {
        return userId in 2854196300L..2854216399L
    }

    @Around("execution(* com.mikuac.yuri.plugins.passive.*.*Handler(..))")
    fun handler(pjp: ProceedingJoinPoint) {
        pjp.args.forEach { arg ->
            when (arg) {
                is AnyMessageEvent -> {
                    arg.let {
                        if (check(it.userId, it.groupId ?: 0L, false)) pjp.proceed()
                        return
                    }
                }

                is GroupMessageEvent -> {
                    arg.let {
                        if (check(it.userId, it.groupId, false)) pjp.proceed()
                        return
                    }
                }

                is PrivateMessageEvent -> {
                    arg.let {
                        if (check(it.userId, 0L, true)) pjp.proceed()
                        return
                    }
                }
            }
        }
        pjp.proceed()
    }

    private fun check(userId: Long, groupId: Long, isPrivate: Boolean): Boolean {
        if (filterTencentBot(userId)) return false
        // 如果用户处于黑名单不响应本次请求
        if (utils.checkUserInBlackList(userId)) return false
        // 如果开启仅白名单模式，则只处理白名单内群组请求
        if (cfg.enableGroupOnlyWhiteList && !isPrivate) {
            if (utils.checkGroupInWhiteList(groupId)) return true
            return false
        }
        // 白名单优先级高于黑名单，如果群组处于白名单则不进行黑名单检查
        if (utils.checkGroupInWhiteList(groupId)) return true
        if (utils.checkGroupInBlackList(groupId)) return false
        return true
    }

}