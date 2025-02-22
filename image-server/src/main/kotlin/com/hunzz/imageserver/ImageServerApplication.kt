package com.hunzz.imageserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class ImageServerApplication

fun main(args: Array<String>) {
    runApplication<ImageServerApplication>(*args)
}
