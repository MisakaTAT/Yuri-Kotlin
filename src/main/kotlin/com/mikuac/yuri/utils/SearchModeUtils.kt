package com.mikuac.yuri.utils

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.shiro.model.ArrayMsg
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Shiro
@Component
class SearchModeUtils {

    data class SearchMode(
        val userId: Long,
        val groupId: Long,
        val mode: String,
        val bot: Bot
    )

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.UNSET_SEARCH_MODE)
    fun unsetSearchMode(bot: Bot, event: AnyMessageEvent) {
        remove(event.userId, event.groupId, bot)
    }

    companion object {
        private val modeNames = hashMapOf("WhatAnime" to "番", "PicSearch" to "图")

        private val expiringMap: ExpiringMap<Long, SearchMode> = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .asyncExpirationListener { _: Long, value: SearchMode -> onExpiration(value) }
            .build()

        // 过期通知
        private fun onExpiration(value: SearchMode) {
            SendUtils.at(
                value.userId,
                value.groupId,
                value.bot,
                "您已经很久没有发送图片啦，帮您退出检索模式了哟～"
            )
        }

        private fun isSearchMode(key: Long): Boolean {
            return expiringMap[key] != null
        }

        fun setSearchMode(mode: String, userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            val info = SearchMode(userId = userId, groupId = groupId, mode = mode, bot)
            val nativeMode: String
            if (isSearchMode(key)) {
                nativeMode = modeNames[expiringMap[key]?.mode].toString()
                SendUtils.at(userId, groupId, bot, "当前已经处于搜${nativeMode}模式啦，请直接发送需要检索的图片。")
                return
            }
            val timeout = Config.plugins.picSearch.timeout.times(1000L)
            expiringMap.put(key, info, timeout, TimeUnit.MILLISECONDS)
            nativeMode = modeNames[mode].toString()
            SendUtils.at(userId, groupId, bot, "您已进入搜${nativeMode}模式，请发送想要查找的图片。")
        }

        private fun resetExpiration(userId: Long, groupId: Long) {
            val key = userId + groupId
            expiringMap.resetExpiration(key)
        }

        private fun remove(userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            expiringMap[key] ?: return
            expiringMap.remove(key)
            SendUtils.at(userId, groupId, bot, "不客气哟！")
        }

        fun check(
            mode: String,
            userId: Long,
            groupId: Long,
        ): Boolean {
            val key = userId + groupId
            // 判断当前检索模式与 SearchModeBean 中是否一致，否则会执行所有检索插件
            if (expiringMap[key]?.mode != mode) return false
            // 判断是否处于搜索模式
            return isSearchMode(key)
        }

        fun getImgUrl(userId: Long, groupId: Long, arrMsg: List<ArrayMsg>): String? {
            val images = arrMsg.filter { it.type == MsgTypeEnum.image }
            if (images.isEmpty()) return null
            // 重新设置过期时间
            resetExpiration(userId, groupId)
            return images[0].data["url"]
        }
    }

}