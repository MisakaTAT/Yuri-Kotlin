@file:Suppress("SpellCheckingInspection")

group = "com.mikuac"

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.jpa") version "1.8.0"
    kotlin("plugin.spring") version "1.8.10"
    kotlin("plugin.allopen") version "1.8.0"

    id("io.freefair.lombok") version "6.6.1"
    // id("org.graalvm.buildtools.native") version "0.9.19"
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
}

java.sourceCompatibility = JavaVersion.VERSION_17

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }
    jar {
        enabled = false
    }
}

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/central")
    }
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springframework.boot:spring-boot-starter-validation")

    api("com.mikuac:shiro:2.0.0")
    api("net.jodah:expiringmap:0.5.10")
    api("cn.hutool:hutool-core:5.8.11")
    api("cn.hutool:hutool-system:5.8.11")
    api("com.google.code.gson:gson:2.10.1")
    api("net.coobird:thumbnailator:0.4.19")
    api("com.github.oshi:oshi-core:6.4.0")
    api("com.google.guava:guava:31.1-jre")
    api("org.telegram:telegrambots:6.4.0")
    api("org.jsoup:jsoup:1.15.3")
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("org.sejda.imageio:webp-imageio:0.1.6")
    api("com.kennycason:kumo-core:1.28")
    api("com.huaban:jieba-analysis:1.0.2")
    api("mysql:mysql-connector-java:8.0.32")

    api("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}