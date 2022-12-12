plugins {
    id("backend.kotlin-application-conventions")
    kotlin("plugin.serialization") version "1.6.0"
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
    maven { url = uri("https://jitpack.io") }
}

val ktorVersion = "1.6.8"

dependencies {
    implementation(project(":db-wrapper"))
    implementation(project(":user-service"))
    implementation(project(":bot-service"))
    implementation(project(":strategy-service"))
    implementation(project(":statistics-service"))

    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

    // Swagger
    implementation("com.github.nielsfalk:ktor-swagger:v0.7.0")
    implementation("com.github.ajalt:clikt:2.8.0")
    implementation("io.ktor:ktor-gson:$ktorVersion")
}

application {
    mainClass.set("backend.server.MainKt")
}