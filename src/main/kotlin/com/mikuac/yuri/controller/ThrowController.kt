package com.mikuac.yuri.controller

import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.plugins.passive.ThrowUser
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.awt.AlphaComposite
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO

@RestController
class ThrowController {

    // http://localhost:5000/throwUser?qq=1140667337
    @RequestMapping(value = ["/throwUser"], produces = [MediaType.IMAGE_PNG_VALUE])
    fun throwUser(@RequestParam qq: Long): BufferedImage? {
        return buildImg(qq)
    }

    private val tempImg = ImageIO.read(ThrowUser::class.java.classLoader.getResource("images/throw.png"))

    @Bean
    fun bufferedImageHttpMessageConverter(): BufferedImageHttpMessageConverter? {
        return BufferedImageHttpMessageConverter()
    }

    private fun buildImg(userId: Long): BufferedImage {
        try {
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
            return Thumbnails.of(tempImg)
                .size(512, 512)
                .asBufferedImage()
        } catch (e: IOException) {
            throw YuriException("图片绘制失败")
        }
    }

}