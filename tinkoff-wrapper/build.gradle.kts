plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    implementation(project(":statistics-service"))

    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("ru.tinkoff.piapi:java-sdk-core:1.0.14")
}