package com.hunzz.moirav1

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.EnableAspectJAutoProxy

@EnableAspectJAutoProxy
@EnableCaching
@SpringBootApplication
class MoiraV1Application

fun main(args: Array<String>) {
    runApplication<MoiraV1Application>(*args)
}
