package com.mikuac.yuri

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableAspectJAutoProxy
@SpringBootApplication
class YuriApplication

fun main(args: Array<String>) {
    runApplication<YuriApplication>(*args)
}
