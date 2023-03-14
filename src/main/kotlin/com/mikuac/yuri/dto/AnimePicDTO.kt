package com.mikuac.yuri.dto

data class AnimePicDTO(
    val data: List<Data>,
    val error: String
) {
    data class Data(
        val author: String,
        val pid: Int,
        val title: String,
        val uid: Int,
        val urls: Urls,
    )

    data class Urls(
        val original: String
    )
}