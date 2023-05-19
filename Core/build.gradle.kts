plugins {
    id("su.nightexpress.excellentenchants.java-conventions")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("net.kyori.indra.git") version "2.1.1"
}

dependencies {
    // The server API
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    // NMS modules
    api(project(":NMS"))
    implementation(project(":V1_17_R1", configuration = "reobf"))
    implementation(project(":V1_18_R2", configuration = "reobf"))
    implementation(project(":V1_19_R2", configuration = "reobf"))
    implementation(project(":V1_19_R3", configuration = "reobf"))

    compileOnly("cc.mewcraft", "mewcore", "5.16.1")

    // 3rd party plugins
    compileOnly("me.clip", "placeholderapi", "2.11.2")
    compileOnly("fr.neatmonster", "nocheatplus", "3.16.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol", "ProtocolLib", "5.0.0")
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.6") {
        exclude("org.bukkit")
    }
    compileOnly("io.lumine", "Mythic-Dist", "5.2.6") {
        isTransitive = false
    }
}

description = "Vanilla-like enchants for your server."
version = "$version".decorateVersion()

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this-${lastCommitHash()}" else this

tasks {
    jar {
        archiveClassifier.set("noshade")
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize {
            exclude(dependency("su.nightexpress.excellentenchants:.*:.*"))
        }
        archiveFileName.set("ExcellentEnchants-${project.version}.jar")
        archiveClassifier.set("")
        destinationDirectory.set(file("$rootDir"))
    }
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to "${project.version}",
                    "description" to project.description
                )
            )
        }
    }
    register("deployJar") {
        doLast {
            exec {
                commandLine("rsync", shadowJar.get().archiveFile.get().asFile.absoluteFile, "dev:data/dev/jar")
            }
        }
    }
    register("deployJarFresh") {
        dependsOn(build)
        finalizedBy(named("deployJar"))
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
