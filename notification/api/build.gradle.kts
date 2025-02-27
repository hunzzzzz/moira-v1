extra["jwtVersion"] = "0.12.6"
extra["springCloudVersion"] = "2024.0.0"

dependencies {
    // common
    implementation(project(":common"))
    // jwt
    implementation("io.jsonwebtoken:jjwt-api:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jwtVersion")}")
    // kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // spring cloud
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
}