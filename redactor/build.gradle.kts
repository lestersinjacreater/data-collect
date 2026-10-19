plugins {
    id("org.jetbrains.kotlin.jvm")
}

// Plain JVM module on purpose: the privacy rules live here and are unit-tested
// without an emulator. The app module (Java 17) consumes it as a normal jar.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
