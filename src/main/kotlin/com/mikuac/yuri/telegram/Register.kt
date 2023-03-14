package com.mikuac.yuri.telegram

import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.plugins.initiative.TelegramForward
import lombok.extern.slf4j.Slf4j
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Slf4j
@Component
class Register : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        val botOptions = DefaultBotOptions()

        val cfg = Config.plugins.telegram
        if (!cfg.enable) {
            return
        }
        if (cfg.botToken.isBlank() || cfg.botUsername.isBlank()) {
            log.error("Telegram 机器人令牌或名称为空")
            return
        }

        val proxy = Config.base.proxy
        val type = DefaultBotOptions.ProxyType.valueOf(proxy.type)
        botOptions.proxyHost = proxy.host
        botOptions.proxyPort = proxy.port
        botOptions.proxyType = type;
        log.info("Telegram 代理启用: {}:{} 代理类型: {}", proxy.host, proxy.port, type)

        try {
            botsApi.registerBot(TelegramForward(botOptions, cfg.botToken))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}