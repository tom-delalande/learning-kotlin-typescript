// package.json
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    application

    kotlin("plugin.serialization") version "1.9.20"
}

// NPM (the repository)
repositories {
    mavenCentral()
}

dependencies {
    // General
    implementation("org.slf4j:slf4j-simple:2.0.0-beta1")

    // Server
    implementation("io.ktor:ktor-client-core-jvm:2.3.8")
    implementation("io.ktor:ktor-server-netty:2.3.8")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
    implementation("io.ktor:ktor-server-status-pages:2.3.8")

    // Database
    implementation("org.postgresql:postgresql:42.7.2")
}

application {
    mainClass.set("app.MainKt")
    applicationName = "app"
}
