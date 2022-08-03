package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.yuri.bean.dto.SauceNaoDto
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.entity.SauceNaoCacheEntity
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.SauceNaoCacheRepository
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SauceNao {

    @Autowired
    private lateinit var repository: SauceNaoCacheRepository

    @Synchronized
    private fun request(imgUrl: String): SauceNaoDto {
        val data: SauceNaoDto
        try {
            val key = ReadConfig.config.plugin.sauceNao.key
            val api = "https://saucenao.com/search.php?api_key=${key}&output_type=2&numres=3&db=999&url=${imgUrl}"
            val result = RequestUtils.get(api).body?.string()
            data = Gson().fromJson(result, SauceNaoDto::class.java)
            if (data.header.longRemaining <= 0) throw YuriException("今日的搜索配额已耗尽啦")
            if (data.header.shortRemaining <= 0) throw YuriException("短时间内搜索配额已耗尽")
            if (data.results.isEmpty()) throw YuriException("未能找到相似的内容")
        } catch (e: Exception) {
            throw YuriException("SauceNao数据获取异常：${e.message}")
        }
        return data
    }

    fun buildMsgForSauceNao(imgUrl: String, imgMd5: String): Pair<String, String> {
        // 查缓存
        val cache = repository.findByMd5(imgMd5)
        if (cache.isPresent) {
            return Pair("", "${cache.get().infoResult}\n[Tips] 该结果为数据库缓存")
        }

        // 返回的结果按相识度排序，第一个相似度最高，默认取第一个
        val resultList = request(imgUrl).results.filter {
            it.header.indexId in listOf(5, 18, 38, 41)
        }
        if (resultList.isEmpty()) {
            return Pair("0", "SauceNao未能找到相似的内容，正在使用Ascii2d进行检索···")
        }
        val result = resultList[0]
        val data = result.data
        val header = result.header
        // 构建消息
        val msgUtils = MsgUtils.builder()
            .img(header.thumbnail)
            .text("\n相似度：${header.similarity}%")
        when (header.indexId) {
            5 -> {
                msgUtils.text("\n标题：${data.title}")
                msgUtils.text("\n画师：${data.authorName}")
                msgUtils.text("\n作品主页：https://pixiv.net/i/${data.pixivId}")
                msgUtils.text("\n画师主页：https://pixiv.net/u/${data.authorId}")
                msgUtils.text("\n反代地址：https://i.loli.best/${data.pixivId}")
                msgUtils.text("\n数据来源：SauceNao (Pixiv)")
            }

            41 -> {
                msgUtils.text("\n链接：${data.extUrls[0]}")
                msgUtils.text("\n用户：" + "https://twitter.com/${data.twitterUserHandle}")
                msgUtils.text("\n数据来源：SauceNao (Twitter)")
            }

            in listOf(18, 38) -> {
                msgUtils.text("\n来源：${data.source}")
                msgUtils.text("\n日文名：${data.jpName}")
                msgUtils.text("\n英文名：${data.engName}")
                msgUtils.text("\n数据来源：SauceNao (H-Misc)")
            }
        }
        val msg = msgUtils.build()
        repository.save(SauceNaoCacheEntity(0, imgMd5, msg))
        return Pair(header.similarity, msg)
    }

}