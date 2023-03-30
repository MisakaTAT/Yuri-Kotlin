package com.mikuac.yuri.plugins.passive

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.Clip
import com.microsoft.playwright.options.Proxy
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.ImageUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class WebScreenshot {

    private val cfg = Config.plugins.webScreenshot

    private val proxy = Config.base.proxy

    private fun screenshot(url: String, fullPage: Boolean, selector: String): String {
        return Playwright.create().use { playwright ->
            val lunchOpts = BrowserType.LaunchOptions()
            if (cfg.proxy) lunchOpts.setProxy(Proxy("${proxy.type.lowercase()}://${proxy.host}:${proxy.port}"))
            playwright.chromium().launch(lunchOpts).use { browser ->
                val context = browser.newContext()
                val page = context.newPage()
                page.navigate(url)
                page.waitForLoadState()
                val pageOpts = Page.ScreenshotOptions()
                if (fullPage) pageOpts.setFullPage(true)
                if (selector.isNotBlank()) {
                    val element = page.querySelector(selector)
                    val box = element.boundingBox()
                    val clip = Clip(box.x, box.y, box.width, box.height)
                    pageOpts.setClip(clip)
                }
                val byteArray = page.screenshot(pageOpts)
                ImageUtils.imgToBase64(byteArray)
            }
        }
    }

    @AnyMessageHandler(cmd = Regex.WEB_SCREENSHOT)
    fun handler(event: AnyMessageEvent, bot: Bot, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            try {
                val action = matcher.group("action")?.trim() ?: ""
                val url = matcher.group("url")?.trim() ?: ""
                val selector = matcher.group("selector")?.trim() ?: ""
                if (url.isBlank()) throw YuriException("URL无效")
                var base64 = ""
                when (action) {
                    "网页截图" -> base64 = screenshot(url, false, selector)
                    "全屏网页截图" -> base64 = screenshot(url, true, selector)
                }
                if (base64.isBlank()) throw YuriException("页面截取失败：$url")
                bot.sendMsg(event, MsgUtils.builder().img(base64).build(), false)
            } catch (e: PlaywrightException) {
                throw RuntimeException(e.message?.let { RegexUtils.group("msg", it, "message='(?<msg>[^'].*)") })
            }
        }
    }

}