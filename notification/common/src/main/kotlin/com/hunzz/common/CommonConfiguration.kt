package com.hunzz.common

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@ComponentScan(value = ["com.hunzz.common"])
@EnableMongoRepositories(basePackages = ["com.hunzz.common"])
@EntityScan(basePackages = ["com.hunzz.common"])
class CommonConfiguration