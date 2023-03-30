package com.mikuac.yuri.plugins.initiative

import com.mikuac.yuri.utils.NetUtils
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class Rss {

    @PostConstruct
    fun a() {
        // RSS源URL列表
        val rssUrls = listOf(
            "https://mikuac.com/feed/",
            "https://rsshub.app/755/user/akimoto-manatsu",
        )

        // 上一次检查的时间戳
        var lastCheckTime = Instant.now()

        while (true) {
            // 检查是否到达检查时间间隔
            val currentTime = Instant.now()
            val elapsed = Duration.between(lastCheckTime, currentTime)
            if (elapsed.toMinutes() < 1) {
                continue
            }

            // 对于每个RSS源，检查是否更新
            for (rssUrl in rssUrls) {
                val feed = NetUtils.get(rssUrl).body?.byteStream().use {
                    SyndFeedInput().build(XmlReader(it))
                }

                println(feed.publishedDate.toInstant())
                println(lastCheckTime)
                if (feed.publishedDate.toInstant().isAfter(lastCheckTime)) {
                    // 如果更新了，输出一条消息
                    println(feed.title)
                    println("${feed.title} 有更新啦！")
                }
            }

            lastCheckTime = currentTime
        }
    }

}