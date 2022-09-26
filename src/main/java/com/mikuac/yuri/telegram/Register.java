package com.mikuac.yuri.telegram;

import com.mikuac.yuri.config.Config;
import com.mikuac.yuri.telegram.plugins.MessageForward;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * @author zero
 */
@Slf4j
@Component
public class Register implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        val botsApi = new TelegramBotsApi(DefaultBotSession.class);
        val botOptions = new DefaultBotOptions();

        if (!Config.plugins.getTelegram().getEnable()) {
            return;
        }

        val proxy = Config.base.getProxy();
        val type = DefaultBotOptions.ProxyType.valueOf(proxy.getType());
        botOptions.setProxyHost(proxy.getHost());
        botOptions.setProxyPort(proxy.getPort());
        botOptions.setProxyType(type);
        log.info("Telegram 代理启用: {}:{} 代理类型: {}", proxy.getHost(), proxy.getPort(), type);

        try {
            botsApi.registerBot(new MessageForward(botOptions));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
