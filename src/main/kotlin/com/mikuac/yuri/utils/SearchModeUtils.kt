package com.mikuac.yuri.utils

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.bean.MsgChainBean
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.bean.SearchModeBean
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SearchModeUtils : BotPlugin() {

    @MessageHandler(cmd = RegexCMD.UNSET_SEARCH_MODE)
    fun unsetSearchMode(bot: Bot, event: WholeMessageEvent) {
        remove(event.userId, event.groupId, bot)
    }

    companion object {

        private val hashMap = hashMapOf("WhatAnime" to "番", "PicSearch" to "图")

        private val expiringMap: ExpiringMap<Long, SearchModeBean> = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .asyncExpirationListener { _: Long, value: SearchModeBean -> expCallBack(value) }
            .build()

        // 过期通知
        private fun expCallBack(value: SearchModeBean) {
            MsgSendUtils.atSend(
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
            val info = SearchModeBean(userId = userId, groupId = groupId, mode = mode, bot)
            val nativeMode: String
            if (isSearchMode(key)) {
                nativeMode = hashMap[expiringMap[key]?.mode].toString()
                MsgSendUtils.atSend(userId, groupId, bot, "当前已经处于搜${nativeMode}模式啦，请直接发送需要检索的图片。")
                return
            }
            val timeout = Config.plugins.picSearch.timeout.times(1000L)
            expiringMap.put(key, info, timeout, TimeUnit.MILLISECONDS)
            nativeMode = hashMap[mode].toString()
            MsgSendUtils.atSend(userId, groupId, bot, "您已进入搜${nativeMode}模式，请发送想要查找的图片。")
        }

        private fun resetExpiration(userId: Long, groupId: Long) {
            val key = userId + groupId
            expiringMap.resetExpiration(key)
        }

        private fun remove(userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            if (expiringMap[key] == null) return
            expiringMap.remove(key)
            MsgSendUtils.atSend(userId, groupId, bot, "不客气哟！")
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
            if (!isSearchMode(key)) return false
            return true
        }

        fun getImgUrl(userId: Long, groupId: Long, arrMsg: List<MsgChainBean>): String? {
            val images = arrMsg.filter { "image" == it.type }
            if (images.isEmpty()) return null
            val imgUrl = images[0].data["url"] ?: return null
            // 重新设置过期时间
            resetExpiration(userId, groupId)
            return imgUrl
        }

    }

}