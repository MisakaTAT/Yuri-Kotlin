package com.mikuac.yuri.config

import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
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
            log.info("当前使用 MySQL 数据库")
            return builder.build()
        }
        builder.driverClassName("org.sqlite.JDBC")
        builder.url("jdbc:sqlite:yuri.sqlite")
        log.info("当前使用 SQLite 数据库")
        return builder.build()
    }

}