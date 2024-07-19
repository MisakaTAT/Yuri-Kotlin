package com.mikuac.yuri.plugins.initiative

import cn.hutool.core.util.IdUtil
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.config.ConfigModel.Plugins.Telegram.Rules.RuleItem
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.utils.BeanUtils
import com.mikuac.yuri.utils.FFmpegUtils
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.TelegramUtils.getFile
import lombok.extern.slf4j.Slf4j
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File
import java.util.function.Supplier
import java.util.stream.Stream

@Slf4j
class TelegramForward(opts: DefaultBotOptions, token: String) : TelegramLongPollingBot(opts, token) {

    private companion object {
        const val GROUP = "group"
        const val PRIVATE = "private"
        const val CHANNEL = "channel"
        const val SUPER_GROUP = "supergroup"
    }

    private val cfg = Config.plugins.telegram

    override fun getBotUsername(): String {
        return cfg.botUsername
    }

    override fun onUpdateReceived(update: Update) {
        // check enable and has message
        if (!cfg.enable || !update.hasMessage()) {
            return
        }

        val message = update.message
        val chat = message.chat
        val type = chat.type
        val title = chat.title ?: ""
        val username = message.from.userName ?: ""

        if (message.hasText()) message.text.trim().takeIf { it.isNotBlank() }?.let {
            MsgUtils.builder().text(it).text("\n${from(username, chat)}").build().let { msg ->
                send(type, msg, username, title)
            }
            return
        }

        if (message.hasPhoto()) photo(message).let { builder ->
            builder.text("\n${from(username, chat)}").build().let {
                send(type, it, username, title)
            }
            return
        }

        if (message.hasVideo()) video(message).trim().takeIf { it.isNotBlank() }?.let {
            send(type, it, username, title)
            send(type, from(username, chat), username, title)
            return
        }

        if (message.hasSticker()) {
            val sticker = message.sticker
            if (sticker.isVideo) videoSticker(message).let { builder ->
                builder.text("\n${from(username, chat)}").build().let {
                    send(type, it, username, title)
                }
                return
            }
            if (!sticker.isAnimated && !sticker.isVideo) imgSticker(message).let { builder ->
                builder.text("\n${from(username, chat)}").build().let {
                    send(type, it, username, title)
                }
                return
            }
        }
    }

    private fun from(username: String, chat: Chat): String {
        return when (chat.type) {
            CHANNEL -> "来自：$username\n频道：${chat.title}"
            GROUP, SUPER_GROUP -> "来自：$username\n群组：${chat.title}"
            else -> "来自：$username"
        }
    }

    private fun photo(message: Message): MsgUtils {
        val builder = MsgUtils.builder()
        message.photo.stream().max(Comparator.comparingInt { obj: PhotoSize -> obj.fileSize }).ifPresent {
            getFile(it.fileId, cfg.proxy).let { url ->
                if (url.isNotBlank()) builder.img(NetUtils.getBase64(url, cfg.proxy))
            }
        }
        message.caption?.takeIf { it.isNotBlank() }?.let {
            builder.text("\n${message.caption}")
        }
        return builder
    }

    private fun video(message: Message): String {
        val builder = MsgUtils.builder()
        return message.video.fileId.let { fileId ->
            getFile(fileId, cfg.proxy).takeIf { it.isNotBlank() }?.let { url ->
                if (!url.endsWith(".mp4")) return builder.build()
                NetUtils.download(
                    url,
                    "cache/telegram",
                    "${IdUtil.simpleUUID()}.mp4",
                    cfg.proxy
                ).let {
                    builder.video("file://${it}", "")
                }
            }
            builder.build()
        }
    }

    private fun imgSticker(message: Message): MsgUtils {
        val builder = MsgUtils.builder()
        return getFile(message.sticker.fileId, cfg.proxy).let { url ->
            if (url.isNotBlank()) builder.img(NetUtils.getBase64(url, cfg.proxy))
            builder
        }
    }

    private fun videoSticker(message: Message): MsgUtils {
        val builder = MsgUtils.builder()
        return message.sticker.fileId.let { fileId ->
            getFile(fileId, cfg.proxy).takeIf { it.isNotBlank() }?.let { url ->
                if (!url.endsWith(".webm")) return builder

                val fileName = "${message.sticker.fileUniqueId}.webm"
                val cachePath = "cache/telegram"
                val file = File(cachePath, fileName)
                if (file.exists()) {
                    builder.img("file://${FFmpegUtils.webm2Gif(file.absolutePath)}")
                } else {
                    NetUtils.download(
                        url,
                        cachePath,
                        fileName,
                        cfg.proxy
                    ).let {
                        builder.img("file://${FFmpegUtils.webm2Gif(it)}")
                    }
                }
            }
            builder
        }
    }

    private fun send(type: String, msg: String, username: String, title: String) {
        listOf(GROUP, SUPER_GROUP).takeIf { it.contains(type) }.let {
            if (cfg.enableUserWhiteList && !cfg.userWhiteList.contains(username)) return
        }

        when (type) {
            PRIVATE -> {
                val target = Supplier<Stream<RuleItem>> { cfg.rules.friend.stream().filter { it.source == username } }
                handler(target, msg)
            }

            CHANNEL -> {
                val target = Supplier<Stream<RuleItem>> { cfg.rules.channel.stream().filter { it.source == title } }
                handler(target, msg)
            }

            GROUP, SUPER_GROUP -> {
                val target = Supplier<Stream<RuleItem>> { cfg.rules.group.stream().filter { it.source == title } }
                handler(target, msg)
            }
        }
    }

    private fun handler(targets: Supplier<Stream<RuleItem>>, msg: String) {
        val bot = BeanUtils.getBean(Global::class.java).bot()
        targets.get().forEach { t ->
            t.target.friend.forEach {
                bot.sendPrivateMsg(it, msg, false)
            }
        }
        targets.get().forEach { t ->
            t.target.group.forEach {
                bot.sendGroupMsg(it, msg, false)
            }
        }
    }

}