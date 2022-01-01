package com.mikuac.yuri.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.utils.CheckUtils
import com.mikuac.yuri.utils.MsgSendUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Aspect
@Component
class BlackListCheckAspect {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    // 过滤腾讯官方机器人
    private fun filterTencentBot(userId: Long): Boolean {
        return userId in 2854196300L..2854216399L
    }

    @Around("execution(* com.mikuac.yuri.plugins.passive.*.*Handler(..))")
    fun handler(pjp: ProceedingJoinPoint) {
        val args = pjp.args
        val event = args[1]
        val bot = args[0] as Bot
        if (event is WholeMessageEvent) {
            if (check(event.userId, event.groupId, bot)) pjp.proceed()
            return
        }
        if (event is GroupMessageEvent) {
            if (check(event.userId, event.groupId, bot)) pjp.proceed()
            return
        }
        if (event is PrivateMessageEvent) {
            if (check(event.userId, 0L, bot)) pjp.proceed()
            return
        }
        pjp.proceed()
    }

    private fun check(userId: Long, groupId: Long, bot: Bot): Boolean {
        if (filterTencentBot(userId)) return false
        if (checkUtils.checkGroupInBlackList(groupId)) return false
        if (checkUtils.checkUserInBlackList(userId)) {
            MsgSendUtils.atSend(userId, groupId, bot, "好好在小黑屋反省吧~")
            return false
        }
        return true
    }

}