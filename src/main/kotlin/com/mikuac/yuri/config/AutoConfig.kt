package com.mikuac.yuri.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.BufferedImageHttpMessageConverter

@Configuration
class AutoConfig {

    @Bean
    fun bufferedImageHttpMessageConverter(): BufferedImageHttpMessageConverter {
        return BufferedImageHttpMessageConverter()
    }

}