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
import org.jsoup.Jsoup
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
        val thread = Thread(this::start)
        thread.start()
    }

    private fun start() {
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
                val entries = feed.entries.first()
                val doc = Jsoup.parse(entries.description.value)
                val images = doc.select("img").map { it.absUrl("src") }
                val msg = MsgUtils.builder()
                    .text("【RSS Subscribe】")
                    .text("\n${entries.title}")
                    .text("\n${entries.link}")
                    .text("\n${DateUtils.format(entries.publishedDate)}")
                if (images.isNotEmpty()) msg.img(images[0])
                cfg.groups.forEach {
                    global.bot().sendGroupMsg(it, msg.build(), false)
                }
            }
            lastCheckTime = currentTime
        }
    }

}
