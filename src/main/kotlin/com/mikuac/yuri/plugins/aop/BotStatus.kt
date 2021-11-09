package com.mikuac.yuri.plugins.aop

import cn.hutool.core.io.FileUtil
import cn.hutool.system.SystemUtil
import cn.hutool.system.oshi.OshiUtil
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.LogUtils
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.enums.RegexEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit


@Component
class BotStatus : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun check(msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.BOT_STATUS.value)) return
        if (!checkUtils.basicCheck(this.javaClass.simpleName, userId, groupId, bot)) return
        buildMsg(userId, groupId, bot)
    }

    private fun buildMsg(userId: Long, groupId: Long, bot: Bot) {
        val upTime = TimeUnit.MILLISECONDS.toMinutes(ManagementFactory.getRuntimeMXBean().uptime)
        val jvmInfo = SystemUtil.getJvmInfo()
        val osInfo = SystemUtil.getOsInfo()
        val processorInfo = OshiUtil.getHardware().processor
        val computerSys = OshiUtil.getHardware().computerSystem
        val runtimeInfo = SystemUtil.getRuntimeInfo()
        val cpuInfo = OshiUtil.getCpuInfo()
        val buildMsg = MsgUtils.builder()
            .text(if (groupId != 0L) "\n" else "")
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
        MsgSendUtils.atSend(userId, groupId, bot, buildMsg)
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        check(event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}