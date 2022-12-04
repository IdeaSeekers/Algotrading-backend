plugins {
    id("backend.kotlin-application-conventions")
    kotlin("plugin.serialization") version "1.6.0"
}

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

dependencies {
    implementation(project(":db-wrapper"))
    implementation(project(":bot-service"))
    implementation(project(":strategy-service"))
    implementation(project(":statistics-service"))

    implementation("io.ktor:ktor-server-netty:1.5.2")
    implementation("io.ktor:ktor-html-builder:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
    implementation("io.ktor:ktor-serialization:1.5.2")
}

application {
    mainClass.set("backend.server.MainKt")
}