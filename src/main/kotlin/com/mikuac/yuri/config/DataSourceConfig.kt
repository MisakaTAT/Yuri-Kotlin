package com.mikuac.yuri.config

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import javax.sql.DataSource

@Configuration
@DependsOn("config")
class DataSourceConfig {

    @Bean
    fun getDataSource(): DataSource {
        val c = Config.base.mysql
        val dataSourceBuilder = DataSourceBuilder.create()
        if (c.enable){
            dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver")
            dataSourceBuilder.url("jdbc:mysql://${c.url}/${c.database}")
            dataSourceBuilder.username(c.username)
            dataSourceBuilder.password(c.password)
            return dataSourceBuilder.build()
        }
        dataSourceBuilder.driverClassName("org.sqlite.JDBC")
        dataSourceBuilder.url("jdbc:sqlite:BotDB.sqlite3")
        return dataSourceBuilder.build()
    }

}