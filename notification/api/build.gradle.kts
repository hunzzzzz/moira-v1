extra["jwtVersion"] = "0.12.6"

dependencies {
    // common
    implementation(project(":common"))
    // jwt
    implementation("io.jsonwebtoken:jjwt-api:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jwtVersion")}")
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
}