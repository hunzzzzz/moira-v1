package com.hunzz.common

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ComponentScan(value = ["com.hunzz.common"])
@EnableCaching
@EnableJpaRepositories(basePackages = ["com.hunzz.common"])
@EntityScan(basePackages = ["com.hunzz.common"])
class CommonConfiguration