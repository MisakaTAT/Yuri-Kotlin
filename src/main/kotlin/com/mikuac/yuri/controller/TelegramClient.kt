package com.mikuac.yuri.controller

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotContainer
import com.mikuac.yuri.bean.dto.TelegramChannelDto
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.utils.TelegramUtils.formatPNG
import com.mikuac.yuri.utils.TelegramUtils.getFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TelegramClient {

    @Autowired
    private lateinit var botContainer: BotContainer

    private var bot: Bot? = null

    @PostMapping("telegram")
    fun receive(@RequestBody data: TelegramChannelDto) {
        val msg = MsgUtils.builder()

        if (data.text.isNotBlank()) {
            msg.text(data.text)
        }

        if (data.photoFileId.isNotBlank()) {
            val file = getFile(data.photoFileId)
            msg.img(file?.let { formatPNG(it) })
        }

        if (data.stickerFileId.isNotBlank()) {
            val file = getFile(data.stickerFileId)
            msg.img(file?.let { formatPNG(it) })
        }

        if (msg.build().isNotBlank()) {
            msg.text("\n\nTG频道：${data.channelTitle}")
            sendGroup(msg.build(), data.channelTitle)
        }

    }

    private fun sendGroup(msg: String, channelTitle: String) {
        if (bot == null) {
            bot = botContainer.robots[Config.base.selfId]!!
            return
        }
        Config.plugins
            .telegram
            .channelRules
            .filter { channelTitle == it.tg }
            .forEach { bot!!.sendGroupMsg(it.qq, msg, false) }
    }

}