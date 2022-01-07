package com.mikuac.yuri.config

import cn.hutool.core.io.watch.SimpleWatcher
import cn.hutool.core.io.watch.WatchMonitor
import cn.hutool.core.io.watch.watchers.DelayWatcher
import com.google.gson.Gson
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Path
import java.nio.file.WatchEvent
import javax.annotation.PostConstruct
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Slf4j
@Component
class ReadConfig {

    val configFileName = "config.yaml"

    companion object {
        lateinit var config: Config
    }

    @PostConstruct
    private fun initConfig() {
        val yaml = Yaml()
        val inputStream = File(configFileName).inputStream()
        val map: HashMap<String, JvmType.Object> = yaml.load(inputStream)
        config = Gson().fromJson(Gson().toJson(map), Config::class.java)
        log.info("配置文件初始化完毕")
    }

    @PostConstruct
    private fun watchMonitorConfigFile() {
        val monitor = WatchMonitor.createAll("./", object : DelayWatcher(object : SimpleWatcher() {
            override fun onModify(event: WatchEvent<*>?, currentPath: Path?) {
                if (configFileName == event?.context().toString()) {
                    initConfig()
                    log.info("配置文件 $configFileName 已重载")
                }
            }
        }, 500) {})
        monitor.start()
    }

}