plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

dependencies {
    compileOnly(project(":NMS"))
    paperweight.paperDevBundle("1.18.2-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}
