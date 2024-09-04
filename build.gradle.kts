@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


group = "com.mikuac"

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.allopen") version "1.9.23"

    id("io.freefair.lombok") version "8.6"
    // id("org.graalvm.buildtools.native") version "0.10.1"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = JavaVersion.VERSION_21.toString()
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.mikuac:shiro:2.3.0")
    implementation("net.jodah:expiringmap:0.5.11")
    implementation("cn.hutool:hutool-core:5.8.26")
    implementation("cn.hutool:hutool-system:5.8.26")
    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("com.github.oshi:oshi-core:6.5.0")
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.kennycason:kumo-core:1.28")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.huaban:jieba-analysis:1.0.2")
    implementation("com.theokanning.openai-gpt3-java:service:0.18.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.graalvm.js:js:23.0.3")
    implementation("org.graalvm.js:js-scriptengine:24.0.0")
    implementation("com.microsoft.playwright:playwright:1.42.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.4.4.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.github.shivam1608:JMailTM:0.8.0")
}