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
        val c = Config.base.mysql
        val dataSourceBuilder = DataSourceBuilder.create()
        if (c.enable) {
            log.info("当前使用 MySQL 数据库")
            dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver")
            dataSourceBuilder.url("jdbc:mysql://${c.url}/${c.database}")
            dataSourceBuilder.username(c.username)
            dataSourceBuilder.password(c.password)
            return dataSourceBuilder.build()
        }
        log.info("当前使用 SQLite3 数据库")
        dataSourceBuilder.driverClassName("org.sqlite.JDBC")
        dataSourceBuilder.url("jdbc:sqlite:BotDB.sqlite3")
        return dataSourceBuilder.build()
    }

}