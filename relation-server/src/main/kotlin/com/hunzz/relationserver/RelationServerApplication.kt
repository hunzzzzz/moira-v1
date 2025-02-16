package com.hunzz.relationserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
class RelationServerApplication

fun main(args: Array<String>) {
    runApplication<RelationServerApplication>(*args)
}
