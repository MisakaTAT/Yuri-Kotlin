package com.mikuac.yuri.utils

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.bean.SearchModeBean
import com.mikuac.yuri.config.ReadConfig
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

class SearchModeUtils {

    companion object {

        private val hashMap = hashMapOf("WhatAnime" to "番", "SauceNao" to "图")

        private val expiringMap: ExpiringMap<Long, SearchModeBean> = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .asyncExpirationListener { _: Long, value: SearchModeBean -> expCallBack(value) }
            .build()

        // 过期通知
        private fun expCallBack(value: SearchModeBean) {
            MsgSendUtils.atSend(value.userId, value.groupId, value.bot, "您已经很久没有发送图片啦，帮您退出检索模式了哟～")
        }

        private fun isSearchMode(key: Long): Boolean {
            return expiringMap[key] != null
        }

        private fun setSearchMode(mode: String, userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            val info = SearchModeBean(userId = userId, groupId = groupId, mode = mode, bot)
            val nativeMode: String
            if (isSearchMode(key)) {
                nativeMode = hashMap[expiringMap[key]?.mode].toString()
                MsgSendUtils.atSend(userId, groupId, bot, "当前已经处于搜${nativeMode}模式啦，请直接发送需要检索的图片。")
                return
            }
            val timeout = ReadConfig.config.base.searchMode.timeout.times(1000L)
            expiringMap.put(key, info, timeout, TimeUnit.MILLISECONDS)
            nativeMode = hashMap[mode].toString()
            MsgSendUtils.atSend(userId, groupId, bot, "您已进入搜${nativeMode}模式，请发送您想查找的图片试试吧～")
        }

        fun resetExpiration(userId: Long, groupId: Long) {
            val key = userId + groupId
            expiringMap.resetExpiration(key)
        }

        private fun remove(userId: Long, groupId: Long, bot: Bot) {
            val key = userId + groupId
            expiringMap.remove(key)
            MsgSendUtils.atSend(userId, groupId, bot, "帮您退出检索模式啦～")
        }

        fun check(
            setRegex: Regex,
            unsetRegex: Regex,
            msg: String,
            mode: String,
            userId: Long,
            groupId: Long,
            bot: Bot
        ): Boolean {
            val key = userId + groupId
            // 进入检索模式
            if (msg.matches(setRegex)) {
                setSearchMode(mode, userId, groupId, bot)
                return false
            }
            // 判断当前检索模式与 SearchModeBean 中是否一致，否则会执行所有检索插件
            if (expiringMap[key]?.mode != mode) return false
            // 退出检索模式
            if (msg.matches(unsetRegex)) {
                remove(userId, groupId, bot)
                return false
            }
            // 判断是否处于搜索模式
            if (!isSearchMode(key)) return false
            return true
        }

    }

}