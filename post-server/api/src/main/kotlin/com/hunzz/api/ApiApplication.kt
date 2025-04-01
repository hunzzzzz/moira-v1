package com.hunzz.api

import com.hunzz.common.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@EnableDiscoveryClient
@EnableScheduling
@Import(value = [CommonConfiguration::class])
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
