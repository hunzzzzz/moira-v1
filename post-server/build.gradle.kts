val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
bootJar.enabled = false

extra["springCloudVersion"] = "2024.0.0"

plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.jpa") version "2.1.0"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

allprojects {
	group = "com.hunzz"
	version = "2.4.0"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	dependencies {
		// cache
		implementation("com.github.ben-manes.caffeine:caffeine")
		// jpa
		implementation("org.springframework.boot:spring-boot-starter-data-jpa")
		// kotlin
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		// redis
		implementation("org.springframework.boot:spring-boot-starter-data-redis")
		// spring cloud
		implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
		implementation("org.springframework.cloud:spring-cloud-starter-bus-kafka")
		implementation("org.springframework.cloud:spring-cloud-starter-config")
		implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
		// test
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	dependencyManagement {
		imports {
			mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		}
	}

	kotlin {
		compilerOptions {
			freeCompilerArgs.addAll("-Xjsr305=strict")
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}
