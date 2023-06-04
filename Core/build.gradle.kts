plugins {
    id("su.nightexpress.project-conventions")
    id("cc.mewcraft.publishing-conventions")
    alias(libs.plugins.indra)
    alias(libs.plugins.shadow)
}

dependencies {
    // The server API
    compileOnly(libs.server.paper)

    // NMS modules
    api(project(":NMS"))
    implementation(project(":V1_18_R2", configuration = "reobf"))
    implementation(project(":V1_19_R3", configuration = "reobf"))

    compileOnly(libs.mewcore)

    // 3rd party plugins
    compileOnly(libs.papi)
    compileOnly(libs.nochestplus)
    compileOnly(libs.protocollib)
    compileOnly(libs.worldguard) {
        exclude("org.bukkit")
    }
    compileOnly(libs.mythicmobs) {
        isTransitive = false
    }
}

description = "Vanilla-like enchants for your server."

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

indra {
    javaVersions().target(17)
}