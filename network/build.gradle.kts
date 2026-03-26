dependencies {
    api(project(":api"))
    implementation("io.netty:netty-all:${property("nettyVersion")}")
    implementation("com.google.code.gson:gson:${property("gsonVersion")}")
}