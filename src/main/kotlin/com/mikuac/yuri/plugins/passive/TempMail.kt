package com.mikuac.yuri.plugins.passive;

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.utils.TempEmailUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Slf4j
@Shiro
@Component
class TempMail {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.TEMP_EMAIL)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        TempEmailUtils.setEmailDataMode(event.userId, event.groupId ?: 0L, bot)
    }
}
