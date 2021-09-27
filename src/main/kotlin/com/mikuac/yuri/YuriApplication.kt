package com.mikuac.yuri

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class YuriApplication

fun main(args: Array<String>) {
    runApplication<YuriApplication>(*args)
}
