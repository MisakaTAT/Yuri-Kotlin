package com.mikuac.yuri.plugins.initiative

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.NetUtils
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Slf4j
@Component
class Rss : ApplicationRunner {

    @Autowired
    private lateinit var global: Global

    private val cfg = Config.plugins.rss

    override fun run(args: ApplicationArguments?) {
        start()
    }

    private fun start() = runBlocking {
        launch {
            log.info("RSS 监听线程已启动")
            var lastCheckTime = Instant.now()
            while (true) {
                val currentTime = Instant.now()
                val elapsed = Duration.between(lastCheckTime, currentTime)
                if (elapsed.toMinutes() < cfg.check) {
                    continue
                }
                cfg.urls.forEach { url ->
                    val feed = NetUtils.get(url).body?.byteStream().use { xml ->
                        SyndFeedInput().build(XmlReader(xml))
                    }
                    if (!feed.publishedDate.toInstant().isAfter(lastCheckTime)) return@forEach
                    val item = feed.entries.first()
                    val msg = MsgUtils.builder()
                        .text("【RSS Subscribe】")
                        .text("\n标题：${item.title}")
                        .text("\n时间：${DateUtils.format(item.publishedDate)}")
                        .text("\n链接：${item.link}")
                        .build()
                    cfg.groups.forEach {
                        global.bot().sendGroupMsg(it, msg, false)
                    }
                }
                lastCheckTime = currentTime
            }
        }
    }

}
