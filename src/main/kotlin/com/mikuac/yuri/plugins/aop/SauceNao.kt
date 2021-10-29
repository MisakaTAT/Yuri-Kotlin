package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.BotPlugin
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.RequestUtils
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class SauceNao : BotPlugin() {

    @PostConstruct
    private fun request() {
        val url = "https://mikuac.com/usr/uploads/2021/07/1090644972.jpeg"
        val key = ReadConfig.config.plugin.sauceNao.key
        val api = "https://saucenao.com/search.php?api_key=${key}&output_type=2&numres=3&db=999&url=${url}"
        val data = RequestUtils.get(api)
        println(data)
    }

}