package com.mikuac.yuri.common.config

data class Config(
    val base: Base,
    val command: Command,
    val plugin: Plugin
) {
    data class Base(
        val adminList: List<Long>,
        val botName: String,
        val botSelfId: Long
    )

    data class Command(
        val prefix: String
    )

    data class Plugin(
        val eroticPic: EroticPic,
        val repeat: Repeat,
        val httpCat: HttpCat
    ) {
        data class EroticPic(
            val api: String,
            val cdTime: Int,
            val recallMsgPicTime: Int
        )

        data class Repeat(
            val thresholdValue: Int
        )

        data class HttpCat(
            val api: String
        )
    }

}
