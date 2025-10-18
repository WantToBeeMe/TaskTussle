import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.0" // kotlin!!
    // if you are using libraries, but they return errors when loading in game, you may want to use this
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
    // and if you do, also build your jars like this ./gradle clean build shadowJar
}

group = "me.wanttobeeme"
version = "1.0.2.21"

bukkitPluginYaml  {
    // https://github.com/jpenilla/resource-factory/tree/master
    main.set("me.wanttobee.tasktussle.MinecraftPlugin")
    apiVersion.set("1.21")
    authors.add("WantToBeeMe")
    description.set("A plugin with a lot of possibilities for all kinds of task based games")
    libraries.add("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20") // kotlin !!

    commands {
        // register("helloWorld") {
        //     usage.set("/helloWorld")
        //     aliases.add("hw")
        //     aliases.add("hello")
        //     description.set("hello world command")
        // }
        register("debug_tt") {
            permission.set("admin.permission")
            usage.set("/debug_tt")
            description.set("debug command for task tussle")
        }
        register("taskTussle") {
            permission.set("admin.permission")
            usage.set("/taskTussle")
            aliases.add("tt")
            description.set("command for task tussle")
        }
        register("lobby") {
            permission.set("admin.permission")
            usage.set("/lobby")
            description.set("lobby stuff, like border, saturation effect, and stuff. just use it you will understand")
        }
    }
}

repositories {
    // mavenLocal()
    // instead of mavenLocal() when you have uploaded it to GitHub and made a release
    mavenCentral()
     maven {
            url = uri("https://jitpack.io") // Use JitPack as a resolver for GitHub releases
        }

    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        // here you can find the latest's spigot versions
        // https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/
    }
}

dependencies {
    implementation("com.github.WantToBeeMe:CommandTree:2.213.0")
    implementation("com.github.WantToBeeMe:EverythingItems:3.2110.1")
    compileOnly("org.spigotmc:spigot-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.20") // kotlin!!
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

// this piece of code makes it so when building a jar with ./gradle clean build shadowJar
// that the 2 jars you will get are ProjectName.jar and ProjectName-1.0.jar
// here the ProjectName.jar is the correct one which you should be using
tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveVersion.set("")
    archiveBaseName.set(project.name)
}
