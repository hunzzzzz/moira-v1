plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	kotlin("plugin.jpa") version "2.1.0"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.hunzz"
version = "2.2.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2024.0.0"

dependencies {
	// aws s3
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.781")
	// database
	runtimeOnly("com.mysql:mysql-connector-j")
	// jpa
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	// image
	implementation("net.coobird:thumbnailator:0.4.20")
	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	// spring boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	// spring cloud
	implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
	implementation("org.springframework.cloud:spring-cloud-starter-bus-kafka")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
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
