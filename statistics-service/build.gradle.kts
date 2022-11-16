plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    implementation(project(":db-wrapper"))
    implementation(project(":common"))
}