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
val solaceSpringBootVersion by extra("2.5.0")
val springShellVersion by extra("3.4.1")
val springGrpcVersion by extra("0.12.0")
val springAiVersion by extra("1.0.3")

dependencies {
    // ========================================
    // Core Spring Boot Starters
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // ========================================
    // Template Engines
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mustache")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-groovy-templates")
    implementation("gg.jte:jte-spring-boot-starter-3:3.1.16")
    implementation("io.github.wimdeblauwe:htmx-spring-boot:4.0.1")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

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
    implementation("org.springframework.security:spring-security-rsocket")
    implementation("org.springframework.security:spring-security-messaging")

    // ========================================
    // Database Migration
    // ========================================
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.flywaydb:flyway-sqlserver")

    // ========================================
    // Data Storage - NoSQL & Graph DB
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-couchbase")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // ========================================
    // Real-time Communication
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-rsocket")

    // ========================================
    // AI & Machine Learning
    // ========================================
    implementation("org.springframework.ai:spring-ai-starter-model-anthropic")

    // ========================================
    // CLI & Shell
    // ========================================
    implementation("org.springframework.shell:spring-shell-starter")

    // ========================================
    // Batch Processing
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // ========================================
    // Message Brokers - Kafka
    // ========================================
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")

    // ========================================
    // Message Brokers - Pulsar
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-pulsar")
    implementation("org.springframework.boot:spring-boot-starter-pulsar-reactive")

    // ========================================
    // Message Brokers - JMS & AMQP
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-activemq")
    implementation("org.springframework.boot:spring-boot-starter-artemis")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // ========================================
    // Message Brokers - Solace
    // ========================================
    implementation("com.solace.spring.boot:solace-spring-boot-starter")

    // ========================================
    // gRPC
    // ========================================
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")

    // ========================================
    // Scheduling
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    // ========================================
    // Integration & Circuit Breaker
    // ========================================
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    implementation("org.springframework.integration:spring-integration-jms")
    implementation("org.springframework.integration:spring-integration-amqp")
    implementation("org.springframework.integration:spring-integration-jdbc")
    implementation("org.springframework.integration:spring-integration-jpa")
    implementation("org.springframework.integration:spring-integration-mongodb")
    implementation("org.springframework.integration:spring-integration-redis")
    implementation("org.springframework.integration:spring-integration-kafka")
    implementation("org.springframework.integration:spring-integration-mail")
    implementation("org.springframework.integration:spring-integration-rsocket")
    implementation("org.springframework.integration:spring-integration-http")
    implementation("org.springframework.integration:spring-integration-websocket")
    implementation("org.springframework.integration:spring-integration-stomp")

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
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.hsqldb:hsqldb")
    runtimeOnly("org.apache.derby:derby")
    runtimeOnly("org.apache.derby:derbytools")
    runtimeOnly("com.oracle.database.jdbc:ojdbc11")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
    runtimeOnly("com.ibm.db2:jcc")

    // ========================================
    // Testing Dependencies
    // ========================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ========================================
    // Custom Libraries
    // ========================================
    implementation("com.github.snowykte0426:peanut-butter:1.4.1")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("com.solace.spring.boot:solace-spring-boot-bom:$solaceSpringBootVersion")
        mavenBom("org.springframework.shell:spring-shell-dependencies:$springShellVersion")
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:$springGrpcVersion")
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
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
