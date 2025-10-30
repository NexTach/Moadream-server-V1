plugins {
    java
    id("org.springframework.boot") version "3.5.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.nextech"
version = "0.0.1"
description = "2025 한전 KDN 소프트웨어 경진대회 출품작"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

spotless {
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        eclipse()
        indentWithSpaces(4)
        importOrder("java", "javax", "org", "com", "")
        removeUnusedImports()
        endWithNewline()
        trimTrailingWhitespace()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

extra["springCloudVersion"] = "2025.0.0"

dependencies {
    // ========================================
    // Core Spring Boot Starters
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // ========================================
    // Template Engines
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // ========================================
    // API & Documentation
    // ========================================
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // ========================================
    // Monitoring & Metrics
    // ========================================
    implementation("io.micrometer:micrometer-registry-prometheus")

    // ========================================
    // Cache
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.hibernate.orm:hibernate-jcache")
    implementation("org.ehcache:ehcache:3.10.8")

    // ========================================
    // Security & Authentication
    // ========================================
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // ========================================
    // Lombok
    // ========================================
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // ========================================
    // Development Tools
    // ========================================
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // ========================================
    // Database Drivers - Runtime Only
    // ========================================
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // ========================================
    // Testing Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ========================================
    // Custom Libraries
    // ========================================
    implementation("com.github.snowykte0426:peanut-butter:1.4.1")
    implementation("com.github.NexTach:mega-method:v1.0.7")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("${project.name}-${project.version}.jar")
    launchScript()
}

tasks.withType<JavaExec> {
    jvmArgs =
        listOf(
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-XX:+UseStringDeduplication",
            "-Xms512m",
            "-Xmx1024m",
        )
}

tasks.named<Jar>("jar") {
    enabled = false
}
