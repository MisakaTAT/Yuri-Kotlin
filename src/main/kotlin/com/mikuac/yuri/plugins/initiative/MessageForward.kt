package com.mikuac.yuri.plugins.initiative

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.config.ConfigData.Plugins.Telegram.Rules.RuleItem
import com.mikuac.yuri.utils.ImageUtils.formatPNG
import com.mikuac.yuri.utils.TelegramUtils.getFile
import lombok.extern.slf4j.Slf4j
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.function.Supplier
import java.util.stream.Stream

@Slf4j
class MessageForward(opts: DefaultBotOptions, token: String, val bot: Bot) : TelegramLongPollingBot(opts, token) {

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

        val msg = MsgUtils.builder()
        if (message.hasText()) {
            msg.text(message.text)
        }

        if (message.hasPhoto()) {
            val photoSizeList = message.photo
            val photo = photoSizeList.stream().max(Comparator.comparingInt { obj: PhotoSize -> obj.fileSize })
            if (photo.isPresent) {
                val file = getFile(photo.get().fileId)
                if (file != null) {
                    msg.img(formatPNG(file, cfg.proxy))
                }
            }
            val caption = message.caption
            if (caption != null && caption.isNotBlank()) {
                msg.text("\n")
                msg.text(message.caption)
            }
        }

        if (message.hasSticker()) {
            val sticker = message.sticker
            // 跳过动画表情和视频
            if (sticker.isAnimated || sticker.isVideo) {
                return
            }
            val file = getFile(sticker.fileId)
            if (file != null) {
                msg.img(formatPNG(file, cfg.proxy))
            }
        }

        val fromUser = message.from.userName
        if (msg.build().isNotBlank()) {
            msg.text("\n发送者：$fromUser")
            when (chat.type) {
                PRIVATE -> msg.text("\nTG私聊：$fromUser")
                CHANNEL -> msg.text("\nTG频道：${chat.title}")
                GROUP, SUPER_GROUP -> msg.text("\nTG群组：${chat.title}")
            }
        }

        // check user white list
        if (listOf(GROUP, SUPER_GROUP).contains(chat.type)) {
            if (cfg.enableUserWhiteList && !cfg.userWhiteList.contains(fromUser)) {
                return
            }
        }

        send(chat.type, msg.build(), fromUser, chat.title)
    }

    private fun send(type: String, msg: String, fromUser: String, title: String) {
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

    private fun handler(targets: Supplier<Stream<RuleItem>>, msg: String) {
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