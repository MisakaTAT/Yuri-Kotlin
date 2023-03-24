package com.mikuac.yuri

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy
@DependsOn("config")
@SpringBootApplication
class YuriApplication

fun main(args: Array<String>) {
    System.setProperty("polyglot.engine.WarnInterpreterOnly", "false")
    runApplication<YuriApplication>(*args)
}
