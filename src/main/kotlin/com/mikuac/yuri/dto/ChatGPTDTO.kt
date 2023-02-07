package com.mikuac.yuri.dto

data class ChatGPTDTO(
    val result: Result,
    val error: Error
) {

    data class Result(
        val choices: List<Choice>?
    ) {
        data class Choice(
            val text: String
        )
    }

    data class Error(
        val error: Error
    ) {
        data class Error(
            val message: String
        )
    }

}


