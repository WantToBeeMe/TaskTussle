plugins {
    kotlin("jvm") version "1.8.0-RC2" // kotlin!!
    kotlin("kapt") version "1.7.10" // kotlin!!
}

group = "me.wanttobeeme"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        // here you can find the latest's spigot versions
        // https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/
    }
}

dependencies {
    // implementation("com.github.WantToBeeMe:wtbmGameLib:0.0.2")
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22") // kotlin!!
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT") // annotations!!
    kapt("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT") // annotations!!
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
