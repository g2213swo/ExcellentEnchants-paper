plugins {
    id("su.nightexpress.project-conventions")
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.indra)
}

dependencies {
    compileOnly(project(":NMS"))
    paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}

indra {
    javaVersions().target(17)
}