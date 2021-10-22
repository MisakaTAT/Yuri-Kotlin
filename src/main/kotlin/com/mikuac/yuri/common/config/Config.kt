package com.mikuac.yuri.common.config

data class Config(
    val base: Base,
    val command: Command,
    val plugin: Plugin
) {
    data class Base(
        val debug: Boolean,
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
        val httpCat: HttpCat,
        val antiBiliMiniApp: AntiBiliMiniApp
    ) {
        data class EroticPic(
            val r18: Boolean,
            val api: String,
            val cdTime: Int,
            val recallMsgPicTime: Int
        )

        data class Repeat(
            val waitTime: Int,
            val thresholdValue: Int
        )

        data class HttpCat(
            val api: String
        )

        data class AntiBiliMiniApp(
            val api: String
        )
    }

}
