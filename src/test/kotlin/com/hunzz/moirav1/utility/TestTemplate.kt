package com.hunzz.moirav1.utility

import com.redis.testcontainers.RedisContainer
import org.junit.Ignore
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Duration

@AutoConfigureMockMvc
@Ignore
@SpringBootTest
@Transactional
class TestTemplate {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

        private val mysqlDB: DockerComposeContainer<*> =
            DockerComposeContainer(File("src/test/resources/docker-compose.yml"))
                .withExposedService(
                    "test-db",
                    3306,
                    Wait.forLogMessage(".*ready for connections.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(3))
                )

        private val redis = RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("6"))

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            mysqlDB.start()
            logger.info("[TestContainers] MySqlDB 컨테이너 시작")

            redis.start()
            logger.info("[TestContainers] Redis 컨테이너 시작")
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            val mysqlHost = mysqlDB.getServiceHost("test-db", 3306)
            val mysqlPort = mysqlDB.getServicePort("test-db", 3306)

            registry.add("spring.datasource.url") { "jdbc:mysql://${mysqlHost}:${mysqlPort}/test-moira" }
            registry.add("spring.datasource.username") { "root" }
            registry.add("spring.datasource.password") { "test-password" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }
            registry.add("spring.jpa.properties.hibernate.dialect") { "org.hibernate.dialect.MySQL8Dialect" }

            val redisHost = redis.host
            val redisPort = redis.firstMappedPort

            registry.add("spring.data.redis.host") { redisHost }
            registry.add("spring.data.redis.port") { redisPort }
        }
    }
}