package com.mikuac.yuri.common.task

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.log.Slf4j.Companion.log
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncTask {

    @Async("taskExecutor")
    fun deleteMsg(msgId: Int, bot: Bot) {
        if (msgId != 0) {
            try {
                // 图片撤回时间，默认为30秒
                Thread.sleep(ReadConfig.config?.plugin?.sexPic?.recallMsgPicTime?.times(1000L) ?: 30000)
                bot.deleteMsg(msgId)
            } catch (e: InterruptedException) {
                log.warn("色图撤回异常: ${e.message}")
            }
        }
    }

}