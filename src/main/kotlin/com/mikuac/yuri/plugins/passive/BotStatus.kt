package com.mikuac.yuri.plugins.passive

import cn.hutool.core.io.FileUtil
import cn.hutool.system.SystemUtil
import cn.hutool.system.oshi.OshiUtil
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.ExceptionHandler
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.text.SimpleDateFormat
import java.util.*

@Shiro
@Component
class BotStatus {

    private fun buildMsg(): String {
        val formatter = SimpleDateFormat("HH:mm:ss")
        formatter.timeZone = TimeZone.getTimeZone("GMT+00:00")
        val upTime = formatter.format(ManagementFactory.getRuntimeMXBean().uptime)
        val jvmInfo = SystemUtil.getJvmInfo()
        val osInfo = SystemUtil.getOsInfo()
        val processorInfo = OshiUtil.getHardware().processor
        val computerSys = OshiUtil.getHardware().computerSystem
        val runtimeInfo = SystemUtil.getRuntimeInfo()
        val cpuInfo = OshiUtil.getCpuInfo()
        return MsgUtils.builder()
            .text("[基本信息]")
            .text("\nBot UpTime: $upTime")
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

    @AnyMessageHandler(cmd = RegexCMD.BOT_STATUS)
    fun handler(bot: Bot, event: AnyMessageEvent) {
        ExceptionHandler.with(bot, event) {
            bot.sendMsg(event, buildMsg(), false)
        }
    }

}