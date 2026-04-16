plugins {
    `java-library`
    application
}

application {
    mainClass.set("dev.ovrex.core.OvrexBootstrap")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "dev.ovrex.core.OvrexBootstrap"
        )
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(configurations.runtimeClasspath)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

dependencies {
    implementation(project(":api"))
    implementation(project(":network"))
    implementation(project(":tower"))
    implementation(project(":plugin"))
    implementation("ch.qos.logback:logback-classic:${property("logbackVersion")}")
    implementation("org.yaml:snakeyaml:${property("snakeYamlVersion")}")
    implementation("com.google.code.gson:gson:${property("gsonVersion")}")
    implementation("io.netty:netty-all:${property("nettyVersion")}")
}