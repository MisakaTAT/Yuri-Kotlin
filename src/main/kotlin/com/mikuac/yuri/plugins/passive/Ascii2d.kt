package com.mikuac.yuri.plugins.passive

import com.alibaba.fastjson.JSONObject
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.yuri.entity.Ascii2dCacheEntity
import com.mikuac.yuri.repository.Ascii2dCacheRepository
import com.mikuac.yuri.utils.RequestUtils
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Shiro
@Component
class Ascii2d {

    @Autowired
    private lateinit var repository: Ascii2dCacheRepository

    fun buildMsgForAscii2d(imgUrl: String, imgMd5: String): Pair<String, String> {
        // 查缓存
        val cache = repository.findByMd5(imgMd5)
        if (cache.isPresent) {
            val json = JSONObject.parseObject(cache.get().infoResult)
            return Pair(
                "${json["color"].toString()}\n[Tips] 该结果为数据库缓存",
                "${json["bovw"].toString()}\n[Tips] 该结果为数据库缓存"
            )
        }

        val colorUrl = RequestUtils.findLink("https://ascii2d.net/search/url/${imgUrl}")

        val colorSearchResult = request(0, colorUrl)
        val bovwSearchResult = request(1, colorUrl.replace("/color/", "/bovw/"))

        val json = JSONObject()
        json["color"] = colorSearchResult
        json["bovw"] = bovwSearchResult

        repository.save(Ascii2dCacheEntity(0, imgMd5, json.toJSONString()))

        return Pair(colorSearchResult, bovwSearchResult)
    }

    private fun request(type: Int, resultUrl: String): String {
        val document = Jsoup.connect(resultUrl).get()
        val itemBox = document.getElementsByClass("item-box")[1]
        val thumbnail = itemBox.select("div.image-box > img")
        val link = itemBox.select("div.detail-box > h6 > a")[0]
        val author = itemBox.select("div.detail-box > h6 > a")[1]

        return MsgUtils.builder()
            .img(thumbnail.attr("abs:src"))
            .text("\n标题：${link.text()}")
            .text("\n作者：${author.text()}")
            .text("\n链接：${link.attr("abs:href")}")
            .text("\n数据来源：Ascii2d ${if (type == 0) "色合検索" else "特徴検索"}")
            .build()
    }

}