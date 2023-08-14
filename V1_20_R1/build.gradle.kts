plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

dependencies {
    compileOnly(project(":NMS"))
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}
repositories {
    mavenCentral()
}