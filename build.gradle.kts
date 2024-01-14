plugins {
    kotlin("jvm") version "1.8.0" // kotlin!!
    kotlin("kapt") version "1.7.10" // kotlin/annotation!!
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.wanttobeeme"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
    // instead of mavenLocal() when you have uploaded it to GitHub and made a release
    // maven {
    //        url = uri("https://jitpack.io") // Use JitPack as a resolver for GitHub releases
    //    }

    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        // here you can find the latest's spigot versions
        // https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/
    }
}

dependencies {
    // implementation("com.github.WantToBeeMe:wtbmGameLib:0.0.2")
    implementation("com.github.WantToBeeMe:CommandTree:1.0")
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22") // kotlin!!
    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT") // annotations!!
    kapt("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT") // annotations!!
}

// Configures a task to process resources, such as a plugin.yml file,
// with version information for the project.
tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// Ensures that both compileJava and compileKotlin tasks will be using Java 1.8,
// aligning Java versions with Kotlin. (because kotlin was automatically be 1.8, while java stayed 1.7 still)
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}