package com.mikuac.yuri.plugins.initiative

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.utils.NetUtils
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant

@Slf4j
@Component
class Rss : ApplicationRunner {

    @Autowired
    private lateinit var global: Global

    private val cfg = Config.plugins.rss

    override fun run(args: ApplicationArguments?) {
        if (cfg.enable) {
            val thread = Thread(this::start)
            thread.start()
        }
    }

    private var lastHashes = mutableMapOf<String, String>()

    private fun String.toMd5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(this.toByteArray())
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun isUpdated(url: String, content: String): Boolean {
        val currentHash = content.toMd5()
        val lastHash = lastHashes[url]
        if (lastHash == null) {
            lastHashes[url] = currentHash
            return false
        }
        lastHashes[url] = currentHash
        return currentHash != lastHash
    }

    private fun start() {
        log.info("RSS 监听线程已启动")
        var lastCheckTime = Instant.now()
        while (true) {
            try {
                val currentTime = Instant.now()
                val elapsed = Duration.between(lastCheckTime, currentTime)
                if (elapsed.toMinutes() < cfg.check) {
                    continue
                }
                log.info("开始检查 RSS 列表更新")
                cfg.urls.forEach { url ->
                    val feed = NetUtils.get(url, cfg.proxy).body?.byteStream().use { xml ->
                        SyndFeedInput().build(XmlReader(xml))
                    }
                    if (feed.entries.isNullOrEmpty()) return@forEach
                    if (!isUpdated(url, feed.entries.first().toString())) return@forEach

                    val entries = feed.entries.first()
                    val doc = Jsoup.parse(entries?.description?.value ?: "")
                    val images = doc.select("img").map { it.absUrl("src") }
                    val msg = MsgUtils.builder()
                        .text("【RSS Subscribe】")
                        .text("\n标题：${entries.title.trim()}")
                        .text("\n链接：${entries.link.trim()}")
                        .text("\n来源：${url}")
                    if (images.isNotEmpty()) msg.img(images[0])
                    cfg.groups.forEach {
                        global.bot().sendGroupMsg(it, msg.build(), false)
                    }
                }
                log.info("RSS 列表更新检查完毕")
                lastCheckTime = currentTime
            } catch (e: Exception) {
                log.warn("RSS 检查更新出现错误：${e.message}")
            }
        }
    }

}
