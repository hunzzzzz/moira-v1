package com.hunzz.data

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
class DataApplication

fun main(args: Array<String>) {
    runApplication<DataApplication>(*args)
}
