package com.hunzz.relationserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class RelationServerApplication

fun main(args: Array<String>) {
    runApplication<RelationServerApplication>(*args)
}
