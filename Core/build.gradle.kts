plugins {
    `java-library`
    // shadow plugin
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

project.ext.set("name", "ExcellentEnchants")
version = "1.0.0-TechBedrock-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://jitpack.io/")
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven {
        url = uri("https://repo.opencollab.dev/main/")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        url = uri("https://maven.citizensnpcs.co/repo")
    }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }

    maven { url = uri("https://repo.md-5.net/content/repositories/snapshots/") }
    maven { url = uri("https://repo.md-5.net/content/repositories/releases/") }
}

dependencies {
    // The server API
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("su.nexmedia:NexEngine:1.0.0-SNAPSHOT")

    // NMS modules
    api(project(":NMS"))

    implementation(project(":V1_18_R2", configuration = "reobf"))
    implementation(project(":V1_19_R3", configuration = "reobf"))
    implementation(project(":V1_20_R1", configuration = "reobf"))

    // 3rd party plugins
    compileOnly("me.clip:placeholderapi:2.11.3")
    compileOnly("fr.neatmonster:nocheatplus:3.16.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9") {
        exclude("org.bukkit")
    }
    compileOnly("io.lumine:Mythic-Dist:5.2.1") { isTransitive = false }

}
