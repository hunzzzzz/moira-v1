dependencies {
    // common
    implementation(project(":common"))
    // database
    runtimeOnly("com.mysql:mysql-connector-j")
    // web
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
}