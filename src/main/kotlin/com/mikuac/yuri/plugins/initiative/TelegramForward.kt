package com.mikuac.yuri.plugins.initiative

import cn.hutool.core.util.IdUtil
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.config.ConfigModel.Plugins.Telegram.Rules.RuleItem
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.utils.BeanUtils
import com.mikuac.yuri.utils.FFmpegUtils
import com.mikuac.yuri.utils.ImageUtils.formatPNG
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.TelegramUtils.getFile
import lombok.extern.slf4j.Slf4j
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.api.objects.Update
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

    @Suppress("kotlin:S3776")
    override fun onUpdateReceived(update: Update) {
        // check enable and has message
        if (!cfg.enable || !update.hasMessage()) {
            return
        }

        val message = update.message
        val chat = message.chat
        val username = message.from.userName

        val items = ArrayList<String>()
        if (message.hasText()) message.text.trim().takeIf { it.isNotBlank() }?.let { items.add(it) }
        if (message.hasPhoto()) photo(message).trim().takeIf { it.isNotBlank() }?.let { items.add(it) }
        if (message.hasVideo()) video(message).trim().takeIf { it.isNotBlank() }?.let { items.add(it) }
        if (message.hasSticker()) {
            val sticker = message.sticker
            if (sticker.isVideo) videoSticker(message).takeIf { it.isNotBlank() }?.let { items.add(it) }
            if (!sticker.isAnimated && !sticker.isVideo) imgSticker(message).takeIf {
                it.isNotBlank()
            }?.let {
                items.add(it)
            }
        }
        items.add(builderFrom(username, chat))

        // check user white list
        listOf(GROUP, SUPER_GROUP).takeIf { it.contains(chat.type) }.let {
            if (cfg.enableUserWhiteList && !cfg.userWhiteList.contains(username)) return
        }

        val msg = ShiroUtils.generateForwardMsg(Config.base.selfId, Config.base.nickname, items)
        send(chat.type, msg, username ?: "", chat.title ?: "")
    }

    private fun builderFrom(username: String, chat: Chat): String {
        val builder = MsgUtils.builder()
        return builder.build().trim().takeIf { it.isNotBlank() }.let {
            when (chat.type) {
                CHANNEL -> builder.text("来自：$username 频道：${chat.title}")
                GROUP, SUPER_GROUP -> builder.text("来自：$username 群组：${chat.title}")
                else -> builder.text("来自：$username")
            }
            builder.build()
        }
    }

    private fun photo(message: Message): String {
        val builder = MsgUtils.builder()
        message.photo.stream().max(Comparator.comparingInt { obj: PhotoSize -> obj.fileSize }).ifPresent {
            getFile(it.fileId, cfg.proxy).let { url ->
                if (url.isNotBlank()) builder.img(formatPNG(url, cfg.proxy))
            }
        }
        message.caption?.takeIf { it.isNotBlank() }.let {
            builder.text("\n")
            builder.text(message.caption)
        }
        return builder.build()
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

    private fun imgSticker(message: Message): String {
        val builder = MsgUtils.builder()
        return getFile(message.sticker.fileId, cfg.proxy).let { url ->
            if (url.isNotBlank()) builder.img(formatPNG(url, cfg.proxy))
            builder.build()
        }
    }

    private fun videoSticker(message: Message): String {
        val builder = MsgUtils.builder()
        return message.sticker.fileId.let { fileId ->
            getFile(fileId, cfg.proxy).takeIf { it.isNotBlank() }?.let { url ->
                if (!url.endsWith(".webm")) return builder.build()
                NetUtils.download(
                    url,
                    "cache/telegram",
                    "${IdUtil.simpleUUID()}.webm",
                    cfg.proxy
                ).let {
                    builder.img("file://${FFmpegUtils.webm2Gif(it)}")
                }
            }
            builder.build()
        }
    }

    private fun send(type: String, msg: List<Map<String, Any>>, fromUser: String, title: String) {
        when (type) {
            PRIVATE -> {
                val target = Supplier<Stream<RuleItem>> { cfg.rules.friend.stream().filter { it.source == fromUser } }
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

    private fun handler(targets: Supplier<Stream<RuleItem>>, msg: List<Map<String, Any>>) {
        val bot = BeanUtils.getBean(Global::class.java).bot()
        targets.get().forEach { t ->
            t.target.friend.forEach {
                bot.sendPrivateForwardMsg(it, msg)
            }
        }
        targets.get().forEach { t ->
            t.target.group.forEach {
                bot.sendGroupForwardMsg(it, msg)
            }
        }
    }

}