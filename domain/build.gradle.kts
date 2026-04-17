plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation("javax.inject:javax.inject:1")
    // Note: If you need coroutines in domain, add kotlinx-coroutines-core to libs.versions.toml
    // implementation(libs.kotlinx.coroutines.core)
}
