package com.mikuac.yuri.common.config

data class Config(
        val base: Base? = null,
        val plugin: Plugin? = null
) {
    data class Base(
            val adminList: List<Long>? = null,
            val botName: String? = null,
            val botSelfId: Long? = null
    )

    data class Plugin(
            val eroticPic: EroticPic? = null
    )

    data class EroticPic(
            val api: String? = null,
            val cdTime: Int? = null,
            val recallMsgPicTime: Int? = null
    )
}
