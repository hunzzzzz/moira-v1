extra["jwtVersion"] = "0.12.6"

dependencies {
    // common
    implementation(project(":common"))
    // coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    // database
    runtimeOnly("com.mysql:mysql-connector-j")
    // jwt
    implementation("io.jsonwebtoken:jjwt-api:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jwtVersion")}")
    // web
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
}