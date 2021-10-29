package com.mikuac.yuri.bean

import com.mikuac.shiro.core.Bot

data class SearchModeBean(
    val userId: Long,
    val groupId: Long,
    val mode: String,
    val bot: Bot
)
