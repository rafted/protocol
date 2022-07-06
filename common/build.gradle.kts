plugins {
    id("java")
    kotlin("jvm") version "1.7.0"
}

dependencies {
    implementation("io.netty:netty-all:4.1.78.Final")
    implementation("net.kyori:adventure-api:4.11.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.11.0")
}
