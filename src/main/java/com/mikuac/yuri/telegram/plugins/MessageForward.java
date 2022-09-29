package com.mikuac.yuri.telegram.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.yuri.config.Config;
import com.mikuac.yuri.config.ConfigDataClass.Plugins.Telegram.Rules.RuleItem;
import com.mikuac.yuri.utils.BeanUtils;
import com.mikuac.yuri.utils.ImageUtils;
import com.mikuac.yuri.utils.TelegramUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author zero
 */
@Slf4j
public class MessageForward extends TelegramLongPollingBot {

    private final static String GROUP = "group";
    private final static String SUPER_GROUP = "supergroup";
    private final static String PRIVATE = "private";
    private final static String CHANNEL = "channel";

    private static Bot bot;

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

        val message = update.getMessage();
        val chat = message.getChat();

        val msg = MsgUtils.builder();

        if (message.hasText()) {
            msg.text(message.getText());
        }

        if (message.hasPhoto()) {
            val photoSizeList = message.getPhoto();
            val photo = photoSizeList.stream().max(Comparator.comparingInt(PhotoSize::getFileSize));
            if (photo.isPresent()) {
                val file = TelegramUtils.getFile(photo.get().getFileId());
                if (file != null) {
                    msg.img(ImageUtils.formatPNG(file));
                }
            }
            val caption = message.getCaption();
            if (caption != null && !caption.isBlank()) {
                msg.text("\n");
                msg.text(message.getCaption());
            }
        }

        if (message.hasSticker()) {
            val sticker = message.getSticker();
            // 跳过动画表情和视频
            if (sticker.getIsAnimated() || sticker.getIsVideo()) {
                return;
            }
            val file = TelegramUtils.getFile(sticker.getFileId());
            if (file != null) {
                msg.img(ImageUtils.formatPNG(file));
            }
        }

        val fromUser = message.getFrom().getUserName();
        if (!msg.build().isBlank()) {
            msg.text("\n发送者：" + fromUser);
            switch (chat.getType()) {
                case PRIVATE -> msg.text("\nTG私聊：" + fromUser);
                case CHANNEL -> msg.text("\nTG频道：" + chat.getTitle());
                case GROUP, SUPER_GROUP -> msg.text("\nTG群组：" + chat.getTitle());
            }
        }

        // check user white list
        if (List.of(GROUP, SUPER_GROUP).contains(chat.getType())) {
            if (config.getEnableUserWhiteList() && !config.getUserWhiteList().contains(fromUser)) {
                return;
            }
        }

        send(chat.getType(), msg.build(), fromUser, chat.getTitle());
    }

    private void send(String type, String msg, String fromUser, String title) {
        if (bot == null) {
            bot = botContainer.robots.get(Config.base.getSelfId());
            return;
        }
        val rules = Config.plugins.getTelegram().getRules();
        switch (type) {
            case PRIVATE -> {
                Supplier<Stream<RuleItem>> target = () -> rules.getFriend().stream().filter(it -> fromUser.equals(it.getSource()));
                handler(target, msg);
            }
            case CHANNEL -> {
                Supplier<Stream<RuleItem>> target = () -> rules.getChannel().stream().filter(it -> title.equals(it.getSource()));
                handler(target, msg);
            }
            case GROUP, SUPER_GROUP -> {
                Supplier<Stream<RuleItem>> target = () -> rules.getGroup().stream().filter(it -> title.equals(it.getSource()));
                handler(target, msg);
            }
        }

    }

    private void handler(Supplier<Stream<RuleItem>> target, String msg) {
        target.get().forEach(it -> it.getTarget().getFriend().forEach(user -> bot.sendPrivateMsg(user, msg, false)));
        target.get().forEach(it -> it.getTarget().getGroup().forEach(group -> bot.sendGroupMsg(group, msg, false)));
    }

}