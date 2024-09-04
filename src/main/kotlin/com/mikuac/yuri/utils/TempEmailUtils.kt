package com.mikuac.yuri.utils

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
import me.shivzee.JMailTM
import me.shivzee.callbacks.EventListener
import me.shivzee.util.Account
import me.shivzee.util.JMailBuilder
import me.shivzee.util.Message
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Shiro
@Component
class TempEmailUtils {

    data class EmailData(
        val userId: Long, val groupId: Long, val bot: Bot, val mailer: JMailTM
    )

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.UNSET_TEMP_EMAIL)
    fun unsetTempEmail(bot: Bot, event: AnyMessageEvent) {
        remove(event.userId, event.groupId ?: 0L, bot)
    }

    companion object {

        private val expiringMap: ExpiringMap<Long, EmailData> =
            ExpiringMap.builder().variableExpiration().expirationPolicy(ExpirationPolicy.CREATED)
                .asyncExpirationListener { _: Long, value: EmailData -> onExpiration(value) }.build()

        private val mailerMap: ConcurrentHashMap<Long, JMailTM> = ConcurrentHashMap();

        // 过期通知
        private fun onExpiration(value: EmailData) {
            val key = value.userId + value.groupId
            mailerMap.remove(key)
            value.mailer.closeMessageListener()
            SendUtils.at(
                value.userId,
                value.groupId,
                value.bot,
                "临时邮${value.mailer.self.email}超时未收到邮件~已自动退出~"
            )
            value.mailer.delete();
        }

        private fun useTempEmailing(key: Long): Boolean {
            return expiringMap[key] != null
        }

        fun setEmailDataMode(userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            if (useTempEmailing(key)) {
                SendUtils.at(userId, groupId, bot, "当前已经处于临时邮箱使用中啦。")
                return
            }
            val mailer = JMailBuilder.createDefault("randomPassword")
            mailer.init()
            mailer.openEventListener(object : EventListener {
                override fun onReady() {
                    val info = EmailData(userId = userId, groupId = groupId, mailer = mailer, bot = bot)
                    val timeout = Config.plugins.tempEmail.timeout.times(1000L)
                    expiringMap.put(key, info, timeout, TimeUnit.MILLISECONDS)
                    SendUtils.at(userId, groupId, bot, "您的临时邮箱账号为：" + mailer.self.email)
                }

                override fun onMessageReceived(message: Message?) {
                    SendUtils.at(
                        userId,
                        groupId,
                        bot,
                        "您的临时邮箱${mailer.self.email}收到来自${message?.senderAddress}的消息：${message?.content}"
                    )
                    resetExpiration(userId, groupId)
                }

                override fun onMessageDelete(id: String?) {
                    SendUtils.at(userId, groupId, bot, "onMessageDelete： ${mailer.self.email}")

                }

                override fun onMessageSeen(message: Message?) {
                    SendUtils.at(userId, groupId, bot, "onMessageSeen： ${mailer.self.email}")

                }

                override fun onAccountDelete(account: Account?) {
                    log.error("email delete === useUser：${userId} email： ${account?.email}")
                }

                override fun onError(error: String?) {
                    SendUtils.at(userId, groupId, bot, "onError：${error}")
                }
            })
        }

        private fun resetExpiration(userId: Long, groupId: Long) {
            val key = userId + groupId
            expiringMap.resetExpiration(key)
        }

        private fun remove(userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            expiringMap[key] ?: return
            expiringMap.remove(key)
            mailerMap[key]?.closeMessageListener()
            mailerMap[key]?.delete()
            mailerMap.remove(key)
            SendUtils.at(userId, groupId, bot, "不客气哟！")
        }


    }


}