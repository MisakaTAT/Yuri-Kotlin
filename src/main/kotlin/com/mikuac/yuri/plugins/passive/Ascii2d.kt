package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.entity.Ascii2dCacheEntity
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.Ascii2dCacheRepository
import com.mikuac.yuri.utils.NetUtils
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.InetSocketAddress
import java.net.Proxy

@Shiro
@Component
class Ascii2d {

    @Autowired
    private lateinit var repository: Ascii2dCacheRepository

    fun buildMsgForAscii2d(imgUrl: String, imgMd5: String): Pair<String, String> {
        // 查缓存
        val cache = repository.findByMd5(imgMd5)
        if (cache.isPresent) {
            val json = Gson().fromJson(cache.get().infoResult, HashMap::class.java)
            return Pair(
                "${json["color"]}\n[Tips] 该结果为数据库缓存", "${json["bovw"]}\n[Tips] 该结果为数据库缓存"
            )
        }

        val proxy = Config.plugins.picSearch.proxy
        val colorUrlResp = NetUtils.get("https://ascii2d.net/search/url/${imgUrl}", proxy)
        val colorUrl = colorUrlResp.request.url.toString()
        colorUrlResp.close()

        try {
            val colorSearchResult = request(0, colorUrl, proxy)
            val bovwSearchResult = request(1, colorUrl.replace("/color/", "/bovw/"), proxy)
            val json = JsonObject()
            json.addProperty("color", colorSearchResult)
            json.addProperty("bovw", bovwSearchResult)
            repository.save(Ascii2dCacheEntity(0, imgMd5, json.toString()))
            return Pair(colorSearchResult, bovwSearchResult)
        } catch (e: IndexOutOfBoundsException) {
            throw YuriException("Ascii2d未检索到相似内容···")
        }

    }

    private fun request(type: Int, resultUrl: String, proxy: Boolean): String {
        val connect = Jsoup.connect(resultUrl)
        if (proxy) {
            val p = Config.base.proxy
            connect.proxy(
                Proxy(Proxy.Type.valueOf(p.type), InetSocketAddress(p.host, p.port))
            )
        }
        val header = connect.header("User-Agent", "PostmanRuntime/7.29.0")
        val document = header.get()

        val itemBox = document.getElementsByClass("item-box")[1]
        val thumbnail = itemBox.select("div.image-box > img")
        val link = itemBox.select("div.detail-box > h6 > a")[0]
        val author = itemBox.select("div.detail-box > h6 > a")[1]

        return MsgUtils
            .builder()
            .img(thumbnail.attr("abs:src"))
            .text("\n标题：${link.text()}")
            .text("\n作者：${author.text()}")
            .text("\n链接：${link.attr("abs:href")}")
            .text("\n数据来源：Ascii2d ${if (type == 0) "色合検索" else "特徴検索"}")
            .build()
    }

}