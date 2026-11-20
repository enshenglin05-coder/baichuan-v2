plugins {
    kotlin("jvm") version "2.1.10"
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
    sourceSets.main {
        kotlin.srcDirs("config", "runtime", "commands")
    }
    sourceSets.test {
        kotlin.srcDirs("src/test/kotlin")
    }
}
