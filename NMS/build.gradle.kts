plugins {
    id("su.nightexpress.project-conventions")
    alias(libs.plugins.indra)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
}

indra {
    javaVersions().target(17)
}
