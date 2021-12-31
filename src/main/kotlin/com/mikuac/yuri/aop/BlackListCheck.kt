package com.mikuac.yuri.aop

import com.alibaba.fastjson.JSONObject
import com.mikuac.yuri.utils.CheckUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Aspect
@Component
class BlackListCheck {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    // 过滤腾讯官方机器人
    private fun filterTencentBot(userId: String): Boolean {
        return userId.toLong() in 2854196300L..2854216399L
    }

    @Around("execution(* com.mikuac.shiro.handler.EventHandler.handler(..))")
    fun handler(pjp: ProceedingJoinPoint) {
        val data = pjp.args[1]
        if (data is JSONObject) {
            val userId = data["user_id"] ?: return
            if (filterTencentBot(userId.toString())) return
            // if (checkUtils.checkUserInBlackList(userId.toString().toLong())) return
            // val groupId = data["group_id"] ?: return
            // if (checkUtils.checkGroupInBlackList(groupId.toString().toLong())) return
            pjp.proceed()
        }
    }

}