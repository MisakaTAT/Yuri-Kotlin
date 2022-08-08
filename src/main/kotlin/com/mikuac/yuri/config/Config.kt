package com.mikuac.yuri.config

import cn.hutool.core.io.watch.SimpleWatcher
import cn.hutool.core.io.watch.WatchMonitor
import cn.hutool.core.io.watch.watchers.DelayWatcher
import com.google.gson.Gson
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.WatchEvent
import javax.annotation.PostConstruct
import kotlin.system.exitProcess

@Slf4j
@Component
class Config {

    private var isReload = false

    private val configFile = "config.jsonc"

    private val defaultConfigFile = "example.config.jsonc"

    companion object {
        lateinit var base: ConfigDataClass.Base
        lateinit var plugins: ConfigDataClass.Plugins
    }

    @Autowired
    private lateinit var ctx: ConfigurableApplicationContext

    @PostConstruct
    private fun initConfig() {
        val defaultConfigFile = File(defaultConfigFile)
        if (!File(configFile).exists()) {
            try {
                log.error("未检测到 config.jsonc 即将从默认配置文件创建")
                FileUtils.copyFile(defaultConfigFile, File(configFile))
                log.info("config.jsoc 创建完成 请修改配置文件后重新启动")
                val exitCode = SpringApplication.exit(ctx, ExitCodeGenerator { 0 })
                exitProcess(exitCode)
            } catch (e: Exception) {
                log.error("从默认配置文件创建失败")
                e.printStackTrace()
            }
        }
        val reader = Files.newBufferedReader(Paths.get(configFile))
        val config = Gson().fromJson(reader, ConfigDataClass::class.java)
        base = config.base
        plugins = config.plugins
        if (!isReload) log.info("配置文件初始化完毕")
    }

    @PostConstruct
    private fun watchMonitorConfigFile() {
        val monitor = WatchMonitor.createAll("./", object : DelayWatcher(object : SimpleWatcher() {
            override fun onModify(event: WatchEvent<*>?, currentPath: Path?) {
                if (configFile == event?.context().toString()) {
                    isReload = true
                    initConfig()
                    log.info("配置文件 $configFile 已重载")
                }
            }
        }, 500) {})
        monitor.start()
    }

}