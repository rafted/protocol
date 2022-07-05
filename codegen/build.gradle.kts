plugins {
    id("java")
    id("application")
    kotlin("jvm") version "1.7.0"
}

dependencies {
    implementation("io.netty:netty-all:4.1.78.Final")
    implementation("com.squareup:kotlinpoet:1.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation(project(":common"))
}

application {
    mainClass.set("MainKt")
}
