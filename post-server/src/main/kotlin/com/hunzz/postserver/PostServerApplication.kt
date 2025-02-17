package com.hunzz.postserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class PostServerApplication

fun main(args: Array<String>) {
    runApplication<PostServerApplication>(*args)
}
