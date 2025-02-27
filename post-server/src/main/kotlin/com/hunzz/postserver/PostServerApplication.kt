package com.hunzz.postserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@SpringBootApplication
class PostServerApplication

fun main(args: Array<String>) {
    runApplication<PostServerApplication>(*args)
}
