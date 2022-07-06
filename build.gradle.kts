import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.7.0"
//    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

allprojects {
    group = "io.github.kraftedmc.protocol"
    version = "1.0"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

}

subprojects {
//    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "java")

    dependencies {
        implementation("io.netty:netty-all:4.1.78.Final")
        implementation("net.kyori:adventure-api:4.11.0")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

}
