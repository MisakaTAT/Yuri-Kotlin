package com.mikuac.yuri.bean.dto

data class ChatGPTDto(
    val choices: List<Choice>,
) {
    data class Choice(
        val text: String
    )
}