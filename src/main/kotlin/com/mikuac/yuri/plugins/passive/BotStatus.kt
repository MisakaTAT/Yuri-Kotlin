package com.mikuac.yuri.plugins.passive

import cn.hutool.core.io.FileUtil
import cn.hutool.system.SystemUtil
import cn.hutool.system.oshi.OshiUtil
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

@Component
class BotStatus : BotPlugin() {

    private fun buildMsg(): String {
        val upTime = TimeUnit.MILLISECONDS.toMinutes(ManagementFactory.getRuntimeMXBean().uptime)
        val jvmInfo = SystemUtil.getJvmInfo()
        val osInfo = SystemUtil.getOsInfo()
        val processorInfo = OshiUtil.getHardware().processor
        val computerSys = OshiUtil.getHardware().computerSystem
        val runtimeInfo = SystemUtil.getRuntimeInfo()
        val cpuInfo = OshiUtil.getCpuInfo()
        return MsgUtils.builder()
            .text("[基本信息]")
            .text("\nBot UpTime: $upTime min")
            .text("\nKotlin Version: ${KotlinVersion.CURRENT}")
            .text("\nJVM Version: ${jvmInfo.version}")
            .text("\nJVM Vendor: ${jvmInfo.vendor}")
            .text("\nJVM Name: ${jvmInfo.name}")
            .text("\n[运行信息]")
            .text("\nCPU Num: ${cpuInfo.cpuNum}")
            .text("\nCPU Used: ${cpuInfo.used}%")
            .text("\nCPU Free: ${cpuInfo.free}%")
            .text("\nMax Memory: ${FileUtil.readableFileSize(runtimeInfo.maxMemory)}")
            .text("\nTotal Memory: ${FileUtil.readableFileSize(runtimeInfo.totalMemory)}")
            .text("\nFree Memory: ${FileUtil.readableFileSize(runtimeInfo.freeMemory)}")
            .text("\nUsable Memory: ${FileUtil.readableFileSize(runtimeInfo.usableMemory)}")
            .text("\n[系统信息]")
            .text("\nOS Name: ${osInfo.name}")
            .text("\nOS Arch: ${osInfo.arch}")
            .text("\nOS Version: ${osInfo.version}")
            .text("\n[硬件信息]")
            .text("\nDevice Model: ${computerSys.model}")
            .text("\nDevice Manufacturer: ${computerSys.manufacturer}")
            .text("\nCPU Name: ${processorInfo.processorIdentifier.name}")
            .text("\nCPU Model: ${processorInfo.processorIdentifier.model}")
            .text("\nMotherBoard Model: ${computerSys.baseboard.model}")
            .text("\nMotherBoard Version: ${computerSys.baseboard.version}")
            .text("\nMotherBoard Manufacturer: ${computerSys.baseboard.manufacturer}")
            .build()
    }

    @MessageHandler(cmd = RegexCMD.BOT_STATUS)
    fun botStatusHandler(bot: Bot, event: WholeMessageEvent) {
        try {
            val msg = buildMsg()
            bot.sendMsg(event, msg, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}