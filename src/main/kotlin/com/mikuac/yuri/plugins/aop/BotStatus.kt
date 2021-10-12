package com.mikuac.yuri.plugins.aop

import cn.hutool.system.SystemUtil
import cn.hutool.system.oshi.OshiUtil
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.MsgSendUtils
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit


@Component
class BotStatus : BotPlugin() {

    private fun buildMsg(userId: Long, groupId: Long, bot: Bot) {
        val upTime = TimeUnit.MILLISECONDS.toMinutes(ManagementFactory.getRuntimeMXBean().uptime)
        val jvmInfo = SystemUtil.getJvmInfo()
        val osInfo = SystemUtil.getOsInfo()
        val cpuInfo = OshiUtil.getHardware().processor
        val computerSys = OshiUtil.getHardware().computerSystem
        val buildMsg = MsgUtils.builder()
            .text(if (groupId != 0L) "\n" else "")
            .text("[基本信息]")
            .text("\nBot UpTime: $upTime min")
            .text("\nKotlin Version: ${KotlinVersion.CURRENT}")
            .text("\nJVM Version: ${jvmInfo.version}")
            .text("\nJVM Vendor: ${jvmInfo.vendor}")
            .text("\nJVM Name: ${jvmInfo.name}")
            .text("\n[系统信息]")
            .text("\nOS Name: ${osInfo.name}")
            .text("\nOS Arch: ${osInfo.arch}")
            .text("\nOS Version: ${osInfo.version}")
            .text("\n[硬件信息]")
            .text("\nDevice Model: ${computerSys.model}")
            .text("\nDevice Manufacturer: ${computerSys.manufacturer}")
            .text("\nCPU Name: ${cpuInfo.processorIdentifier.name}")
            .text("\nCPU Model: ${cpuInfo.processorIdentifier.model}")
            .text("\nMotherBoard Model: ${computerSys.baseboard.model}")
            .text("\nMotherBoard Version: ${computerSys.baseboard.version}")
            .text("\nMotherBoard Manufacturer: ${computerSys.baseboard.manufacturer}")
            .build()
        MsgSendUtils.sendAll(userId, groupId, bot, buildMsg)
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        if (event.message.equals("a")) {
            buildMsg(event.userId, 0L, bot)
        }
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        if (event.message.equals("a")) {
            buildMsg(event.userId, event.groupId, bot)
        }
        return MESSAGE_IGNORE
    }

}