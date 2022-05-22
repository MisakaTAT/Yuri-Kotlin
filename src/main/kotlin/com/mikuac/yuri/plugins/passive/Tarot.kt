package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.bean.TarotDataBean
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.MsgSendUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Shiro
@Component
class Tarot : ApplicationRunner {

    private val expiringMap: ExpiringMap<Long, Boolean> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(ReadConfig.config.plugin.tarot.cdTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    companion object {
        lateinit var data: TarotDataBean
    }

    override fun run(args: ApplicationArguments?) {
        val yaml = Yaml()
        val stream = javaClass.classLoader.getResourceAsStream("tarot.yaml")!!
        val bufferedReader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        val map: HashMap<String, JvmType.Object> = yaml.load(bufferedReader)
        data = Gson().fromJson(Gson().toJson(map), TarotDataBean::class.java)
    }

    private fun buildMsg(msgId: Int): String {
        val data = data.tarot[Random().nextInt(data.tarot.size)]
        val desc = if (Random().nextInt(100) and 1 != 0) "[正位] ${data.positive}" else "[逆位] ${data.negative}"
        return MsgUtils.builder()
            .text(data.name)
            .text("\n\n$desc")
            .img("https://mikuac.com/images/tarot/${data.imageName}")
            .reply(msgId)
            .build()
    }

    @MessageHandler(cmd = RegexCMD.TAROT)
    fun tarotHandler(bot: Bot, event: WholeMessageEvent) {
        try {
            val userId = event.userId
            if (expiringMap[userId] != null && expiringMap[userId] == true) {
                val expectedExpiration = expiringMap.getExpectedExpiration(userId) / 1000
                throw YuriException("塔罗牌抽取剩余冷却时间 $expectedExpiration 秒")
            }
            bot.sendMsg(event, buildMsg(event.messageId), false)
            expiringMap[userId] = true
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}