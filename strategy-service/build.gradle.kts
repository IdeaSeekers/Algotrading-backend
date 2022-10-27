plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    api(project(":tinkoff-wrapper"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}