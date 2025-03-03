package com.hunzz.cache

import com.hunzz.common.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import

@EnableCaching
@EnableDiscoveryClient
@Import(value = [CommonConfiguration::class])
@SpringBootApplication
class CacheApplication

fun main(args: Array<String>) {
    runApplication<CacheApplication>(*args)
}
