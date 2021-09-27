package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.PluginSwitchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface PluginSwitchRepository : JpaRepository<PluginSwitchEntity, String> {

    fun findByPluginName(pluginName: String): Optional<PluginSwitchEntity>

}