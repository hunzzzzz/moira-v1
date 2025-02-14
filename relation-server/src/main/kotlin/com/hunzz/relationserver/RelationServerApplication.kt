package com.hunzz.relationserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.scheduling.annotation.EnableScheduling

@EnableDiscoveryClient
@EnableScheduling
@SpringBootApplication
class RelationServerApplication

fun main(args: Array<String>) {
    runApplication<RelationServerApplication>(*args)
}
