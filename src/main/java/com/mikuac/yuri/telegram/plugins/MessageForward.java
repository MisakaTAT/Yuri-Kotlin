package com.mikuac.yuri.telegram.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.yuri.config.Config;
import com.mikuac.yuri.utils.BeanUtils;
import com.mikuac.yuri.utils.TelegramUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;

/**
 * @author zero
 */
@Slf4j
public class MessageForward extends TelegramLongPollingBot {

    public MessageForward(DefaultBotOptions options) {
        super(options);
    }

    private final BotContainer botContainer = BeanUtils.getBean(BotContainer.class);

    @Override
    public String getBotUsername() {
        return Config.plugins.getTelegram().getBotUsername();
    }

    @Override
    public String getBotToken() {
        return Config.plugins.getTelegram().getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        val config = Config.plugins.getTelegram();

        // check enable and has message
        if (!config.getEnable() || !update.hasMessage()) {
            return;
        }

        val chat = update.getMessage().getChat();
        val fromUser = update.getMessage().getFrom().getUserName();

        // check user white list
        if (config.getEnableUserWhiteList() && !config.getUserWhiteList().contains(fromUser)) {
            return;
        }

        val msg = MsgUtils.builder();

        if (update.getMessage().hasText()) {
            msg.text(update.getMessage().getText());
        }

        if (update.getMessage().hasPhoto()) {
            val photoSizeList = update.getMessage().getPhoto();
            val photo = photoSizeList.stream().max(Comparator.comparingInt(PhotoSize::getFileSize));
            if (photo.isPresent()) {
                val file = TelegramUtils.getFile(photo.get().getFileId());
                if (file != null) {
                    msg.img(TelegramUtils.formatPNG(file));
                }
            }
        }

        if (update.getMessage().hasSticker()) {
            val sticker = update.getMessage().getSticker();
            // 跳过动画表情和视频
            if (sticker.getIsAnimated() || sticker.getIsVideo()) {
                return;
            }
            val file = TelegramUtils.getFile(sticker.getFileId());
            if (file != null) {
                msg.img(TelegramUtils.formatPNG(file));
            }
        }

        if (!msg.build().isBlank()) {
            val groupTitle = chat.getTitle();
            // 私聊为 null
            if (groupTitle == null) {
                return;
            }
            msg.text("\n发送者：" + fromUser);
            msg.text("\nTG群组：" + groupTitle);
            sendGroup(msg.build(), groupTitle);
        }
    }

    private void sendGroup(String msg, String groupTitle) {
        val bot = botContainer.robots.get(Config.base.getSelfId());
        if (bot == null) {
            log.error("Telegram MessageForward bot instance is null");
            return;
        }
        Config.plugins
                .getTelegram()
                .getRules()
                .stream()
                .filter(it -> groupTitle.equals(it.getTg()))
                .forEach(it -> bot.sendGroupMsg(it.getQq(), msg, false));
    }

}
