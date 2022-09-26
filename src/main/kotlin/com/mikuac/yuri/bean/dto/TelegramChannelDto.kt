package com.mikuac.yuri.bean.dto

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
data class TelegramChannelDto(

    @JsonProperty("channel_title")
    val channelTitle: String,

    @JsonProperty("text")
    val text: String,

    @JsonProperty("sticker_file_id")
    val stickerFileId: String,

    @JsonProperty("photo_file_id")
    val photoFileId: String,

    )