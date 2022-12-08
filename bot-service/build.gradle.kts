plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    api(project(":db-wrapper"))
    api(project(":common"))
    api(project(":strategy-service"))
}