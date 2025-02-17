package com.hunzz.postserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableCaching
@EnableDiscoveryClient
@SpringBootApplication
class PostServerApplication

fun main(args: Array<String>) {
    runApplication<PostServerApplication>(*args)
}
