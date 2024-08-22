package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.springframework.stereotype.Component
import java.awt.AlphaComposite
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

@Shiro
@Component
class ThrowUser {

    private val tempImg = ImageIO.read(ThrowUser::class.java.classLoader.getResource("images/throw.png"))

    private fun drawImage(userId: Long): String {
        // 从 AvatarURL 读入头像图片
        val image = ImageIO.read(URL(ShiroUtils.getUserAvatar(userId, 640)))
        // 设置图层参数
        val avaImg = BufferedImage(image.width, image.height, BufferedImage.TYPE_4BYTE_ABGR)
        // 开启抗锯齿
        val renderingHints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        // 绘制蒙版
        val shape = Ellipse2D.Double(.0, .0, image.width.toDouble(), image.height.toDouble())
        val avaImgGraphics = avaImg.createGraphics()
        // 绘制开始
        avaImgGraphics.setRenderingHints(renderingHints)
        avaImgGraphics.clip = shape
        avaImgGraphics.drawImage(image, 0, 0, null)
        avaImgGraphics.dispose()
        // 绘制结束
        // 旋转图片
        var proceedAva = Thumbnails.of(avaImg)
            .size(136, 136)
            .rotate(-160.0)
            .asBufferedImage()
        // 二次裁切旋转后图片
        proceedAva = Thumbnails.of(proceedAva)
            .sourceRegion(Positions.CENTER, 136, 136)
            .size(136, 136)
            .keepAspectRatio(false)
            .asBufferedImage()
        val bgImgGraphics = tempImg.createGraphics()
        bgImgGraphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP)
        bgImgGraphics.drawImage(proceedAva, 19, 181, 137, 137, null)
        // 结束绘制图片
        bgImgGraphics.dispose()

        val stream = ByteArrayOutputStream()
        ImageIO.write(Thumbnails.of(tempImg).size(512, 512).asBufferedImage(), "PNG", stream)
        return Base64.getEncoder().encodeToString(stream.toByteArray())
    }

    @Suppress("kotlin:S6611")
    private fun buildMsg(event: GroupMessageEvent): String {
        val atList = event.arrayMsg.filter { it.type == MsgTypeEnum.at }
        if (atList.isEmpty()) return ""
        val atUserId = atList[0].data["qq"]!!
        if ("all" == atUserId) throw YuriException("哼哼～ 没想到你个笨蛋还想把所有人都丢出去")
        return "base64://${drawImage(atUserId.toLong())}"
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = Regex.THROW_USER)
    fun handler(bot: Bot, event: GroupMessageEvent) {
        ExceptionHandler.with(bot, event) {
            buildMsg(event).let {
                if (it.isNotBlank()) bot.sendGroupMsg(event.groupId, MsgUtils.builder().img(it).build(), false)
            }
        }
    }

}