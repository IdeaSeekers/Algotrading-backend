plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    implementation("org.jetbrains.exposed:exposed-jdbc:0.38.2")
    implementation("org.slf4j:slf4j-simple:1.7.9")
    implementation("org.postgresql:postgresql:42.2.2")
}