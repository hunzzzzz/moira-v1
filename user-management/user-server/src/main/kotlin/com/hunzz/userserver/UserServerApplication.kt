package com.hunzz.userserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class UserServerApplication

fun main(args: Array<String>) {
    runApplication<UserServerApplication>(*args)
}
