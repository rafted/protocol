import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.7.0"
}

group = "io.github.kraftedmc.protocol"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-all:4.1.78.Final")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}