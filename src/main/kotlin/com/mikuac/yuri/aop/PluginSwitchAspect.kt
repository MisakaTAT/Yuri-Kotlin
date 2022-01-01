package com.mikuac.yuri.aop

import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(1)
class PluginSwitchAspect {

//    @Around("execution(* com.mikuac.yuri.plugins.passive.*.handler(..))")
//    fun handler(pjp: ProceedingJoinPoint) {
//        val pluginName = pjp.signature.declaringType.simpleName
//    }

}