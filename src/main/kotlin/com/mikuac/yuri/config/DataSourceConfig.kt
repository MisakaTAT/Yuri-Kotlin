package com.mikuac.yuri.config

import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.exception.YuriException
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import javax.sql.DataSource

@Slf4j
@Configuration
@DependsOn("config")
class DataSourceConfig {

    @Bean
    fun getDataSource(): DataSource {
        val cfg = Config.base.mysql
        val builder = DataSourceBuilder.create()
        if (cfg.enable) {
            builder.driverClassName("com.mysql.cj.jdbc.Driver")
            builder.url("jdbc:mysql://${cfg.url}/${cfg.database}")
            builder.username(cfg.username)
            builder.password(cfg.password)
            return builder.build()
        }
        // 计划重新支持 SQLite3
        throw YuriException("请正确配置并启用数据库")
    }

}