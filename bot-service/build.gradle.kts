plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    api(project(":common"))
    api(project(":strategy-service"))
}