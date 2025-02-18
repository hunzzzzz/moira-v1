package com.hunzz.feedserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class FeedServerApplication

fun main(args: Array<String>) {
    runApplication<FeedServerApplication>(*args)
}
