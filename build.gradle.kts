import org.gradle.api.JavaVersion

plugins {
    java
}

allprojects {
    group = "dev.ovrex"
    version = "26.0-snapshot-1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")
        implementation("org.slf4j:slf4j-api:2.0.9")
    }
}