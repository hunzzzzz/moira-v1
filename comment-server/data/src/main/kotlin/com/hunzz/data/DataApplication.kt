package com.hunzz.data

import com.hunzz.common.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(value = [CommonConfiguration::class])
@SpringBootApplication
class DataApplication

fun main(args: Array<String>) {
    runApplication<DataApplication>(*args)
}
