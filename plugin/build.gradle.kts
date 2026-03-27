dependencies {
    api(project(":api"))
    implementation("org.yaml:snakeyaml:${property("snakeYamlVersion")}")
}