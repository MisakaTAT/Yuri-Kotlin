package com.mikuac.yuri.plugins.passive

import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.CircleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.LinearFontScalar
import com.kennycason.kumo.image.AngleGenerator
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.entity.WordCloudEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.WordCloudRepository
import com.mikuac.yuri.utils.MsgSendUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.regex.Matcher

@Shiro
@Component
class WordCloud {

    @Autowired
    private lateinit var repository: WordCloudRepository

    @GroupMessageHandler
    fun saveMsg(event: GroupMessageEvent) {
        repository.save(
            WordCloudEntity(0, event.userId, event.groupId, event.message, LocalDate.now())
        )
    }

    fun generateWordCloud(text: List<String>): String {
        val frequencyAnalyzer = FrequencyAnalyzer()
        frequencyAnalyzer.setWordFrequenciesToReturn(300)
        frequencyAnalyzer.setMinWordLength(2)
        frequencyAnalyzer.setWordTokenizer(ChineseWordTokenizer())
        val wordFrequencies = frequencyAnalyzer.load(text)
        val dimension = Dimension(1000, 1000)
        val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)
        wordCloud.setPadding(2)
        wordCloud.setAngleGenerator(AngleGenerator(0))
        wordCloud.setKumoFont(
            KumoFont(this.javaClass.getResourceAsStream("/font/loli.ttf"))
        )
        val colors = mutableListOf("0000FF", "40D3F1", "40C5F1", "40AAF1", "408DF1", "4055F1")
        wordCloud.setBackground(CircleBackground(((1000 + 1000) / 4)))
        wordCloud.setBackgroundColor(Color(0xFFFFFF))
        wordCloud.setColorPalette(
            ColorPalette(colors.map { it.toIntOrNull(16)?.let { it1 -> Color(it1) } })
        )
        wordCloud.setFontScalar(
            LinearFontScalar(
                ReadConfig.config.plugin.wordCloud.minFontSize, ReadConfig.config.plugin.wordCloud.maxFontSize
            )
        )
        wordCloud.build(wordFrequencies)

        val stream = ByteArrayOutputStream()
        wordCloud.writeToStreamAsPNG(stream)

        return Base64.getEncoder().encodeToString(stream.toByteArray())
    }

    fun query(userId: Long, groupId: Long, start: LocalDate, end: LocalDate): List<String> {
        return repository.findAllBySenderIdAndGroupIdAndTimeBetween(userId, groupId, start, end).map { it.content }
            .toList()
    }

    fun query(groupId: Long, start: LocalDate, end: LocalDate): List<String> {
        return repository.findAllByGroupIdAndTimeBetween(groupId, start, end).map { it.content }.toList()
    }

    fun getWordsForRange(userId: Long, groupId: Long, type: String, range: String): List<String> {
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
        val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        val startOfYear = today.with(TemporalAdjusters.firstDayOfYear())
        val endOfYear = today.with(TemporalAdjusters.lastDayOfYear())

        when (type) {
            "我的" -> {
                when (range) {
                    "今日" -> return query(userId, groupId, today, today)
                    "本周" -> return query(userId, groupId, startOfWeek, endOfWeek)
                    "本月" -> return query(userId, groupId, startOfMonth, endOfMonth)
                    "本年" -> return query(userId, groupId, startOfYear, endOfYear)
                }
            }

            "本群" -> {
                when (range) {
                    "今日" -> return query(groupId, today, today)
                    "本周" -> return query(groupId, startOfWeek, endOfWeek)
                    "本月" -> return query(groupId, startOfMonth, endOfMonth)
                    "本年" -> return query(groupId, startOfYear, endOfYear)
                }
            }
        }
        return listOf()
    }

    @GroupMessageHandler(cmd = RegexCMD.WORD_CLOUD)
    fun wordCloudHandler(event: GroupMessageEvent, bot: Bot, matcher: Matcher) {
        val msgId = event.messageId
        try {
            val type = matcher.group(1)
            val range = matcher.group(2)

            MsgSendUtils.replySend(msgId, event.userId, event.groupId, bot, "数据检索中，请耐心等待～")

            val contents = ArrayList<String>()
            getWordsForRange(event.userId, event.groupId, type, range).forEach { raw ->
                contents.addAll(
                    ShiroUtils.stringToMsgChain(raw).filter { it.type == "text" }.map {
                        it.data["text"]!!.trim()
                    }.filter { !it.contains("http|词云|&#".toRegex()) }.toList()
                )
            }

            if (contents.isEmpty()) {
                throw YuriException("唔呣～数据库里没有找到你的发言记录呢")
            }

            val msg = MsgUtils.builder().reply(msgId).img("base64://${generateWordCloud(contents)}").build()
            bot.sendGroupMsg(event.groupId, msg, false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(msgId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(msgId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}
