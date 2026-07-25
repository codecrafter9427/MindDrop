plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // api, not implementation: all three appear in :domain's public API --
    // repositories return Flow and PagingData, and use cases / qualifiers are
    // annotated with javax.inject. With implementation they aren't visible
    // transitively, so a consumer can see IoDispatcher without being able to
    // tell it's a @Qualifier. That worked only by accident while :data and :app
    // happened to declare the same libraries themselves.
    api(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)
    api(libs.androidx.paging.common)
    testImplementation(libs.junit)
}
