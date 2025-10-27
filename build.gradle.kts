plugins {
    java
    id("org.springframework.boot") version "3.4.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.nextech"
version = "0.0.1-SNAPSHOT"
description = "2025 한전 KDN 소프트웨어 경진대회 출품작"

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

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.2"

dependencies {
    // --- Spring Boot Starters -------------------------------------------------
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // --- Spring Cloud / Feign ------------------------------------------------
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // --- Template / View helpers ---------------------------------------------
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // --- Lombok (compile only + annotation processor) ------------------------
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // --- Development-only utilities -----------------------------------------
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // --- Runtime-only -------------------------------------------------------
    runtimeOnly("com.h2database:h2")

    // --- Testing ------------------------------------------------------------
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
