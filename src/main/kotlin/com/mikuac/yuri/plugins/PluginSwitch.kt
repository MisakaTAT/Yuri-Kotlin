package com.mikuac.yuri.plugins

import com.mikuac.yuri.common.log.Slf4j.Companion.log
import com.mikuac.yuri.entity.PluginSwitchEntity
import com.mikuac.yuri.repository.PluginSwitchRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class PluginSwitch {

    @Autowired
    private lateinit var repository: PluginSwitchRepository

    @PostConstruct
    fun init() {
        val pluginList = listOf("Poke", "EroticPic")
        pluginList.forEach {
            if (repository.findByPluginName(it).isPresent) {
                log.info("插件开关表 {} 插件已初始化，即将跳过此项", it)
            } else {
                log.warn("插件开关表 {} 插件不存在，即将初始化", it)
                repository.save(PluginSwitchEntity(0, it, false))
            }
        }
    }

}
