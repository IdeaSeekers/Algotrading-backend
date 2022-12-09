plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    api(project(":common"))
    implementation(project(":bot-service"))
    implementation(project(":tinkoff-wrapper"))

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}