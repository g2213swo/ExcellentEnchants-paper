plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroup("io.papermc.paper")
            includeGroup("net.md-5") // bungeecord-chat-1.16-R0.4-deprecated is required by paper-api
        }
    }
    maven("https://maven.enginehub.org/repo/") {
        content {
            includeGroup("com.sk89q.worldguard")
            includeGroup("com.sk89q.worldguard.worldguard-libs")
            includeGroup("com.sk89q.worldedit")
            includeGroup("com.sk89q.worldedit.worldedit-libs")
        }
    }
    maven("https://repo.dmulloy2.net/repository/public/") {
        content {
            includeGroup("com.comphenix.protocol")
        }
    }
    maven("https://repo.md-5.net/content/repositories/snapshots/") {
        // See doc: https://github.com/NoCheatPlus/Docs/wiki/API
        content {
            includeGroup("fr.neatmonster")
        }
    }
    maven("https://mvn.lumine.io/repository/maven-public/") {
        content {
            includeGroup("io.lumine")
        }
    }
}

dependencies {
    compileOnly("su.nexmedia:NexEngine:2.2.10")
    compileOnly("org.jetbrains:annotations:24.0.0")
}

group = "su.nightexpress.excellentenchants"
version = "3.4.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
}
