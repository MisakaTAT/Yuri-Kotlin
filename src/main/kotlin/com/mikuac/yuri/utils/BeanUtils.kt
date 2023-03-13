package com.mikuac.yuri.utils

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class BeanUtils : ApplicationContextAware {

    companion object {
        private var ctx: ApplicationContext? = null

        fun getBean(beanName: String): Any {
            return ctx!!.getBean(beanName)
        }

        fun <T> getBean(c: Class<T>): T {
            return ctx!!.getBean(c)
        }

        fun <T> getBean(name: String, c: Class<T>): T {
            return ctx!!.getBean(name, c)
        }

        fun getCtx(): ApplicationContext {
            return ctx!!
        }
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        if (ctx == null) {
            ctx = applicationContext
        }
    }

}