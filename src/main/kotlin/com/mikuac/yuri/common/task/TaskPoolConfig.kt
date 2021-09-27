package com.mikuac.yuri.common.task

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
class TaskPoolConfig {

    companion object {
        /**
         * 核心线程数（默认线程数）
         */
        const val CORE_POOL_SIZE: Int = 10

        /**
         * 最大线程数
         */
        const val MAX_POOL_SIZE: Int = 30

        /**
         * 允许线程空闲时间（单位：默认为秒）
         */
        const val KEEP_ALIVE_TIME: Int = 10

        /**
         * 缓冲队列大小
         */
        const val QUEUE_CAPACITY: Int = 200

        /**
         * 线程池名前缀
         */
        const val THREAD_NAME_PREFIX: String = "YuriTaskPool-"

    }

    @Bean("taskExecutor")
    fun taskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = CORE_POOL_SIZE
        executor.maxPoolSize = MAX_POOL_SIZE
        executor.setQueueCapacity(QUEUE_CAPACITY)
        executor.keepAliveSeconds = KEEP_ALIVE_TIME
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX)
        executor.setRejectedExecutionHandler(object : ThreadPoolExecutor.CallerRunsPolicy() {})
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true)
        // 初始化
        executor.initialize()
        return executor
    }

}