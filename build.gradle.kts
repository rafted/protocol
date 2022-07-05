import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.7.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

allprojects {
    group = "io.github.kraftedmc.protocol"
    version = "1.0"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

//    ktlint {
//    }
}
