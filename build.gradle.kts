plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

group = "fr.cykalos"
version = (findProperty("releaseVersion") as String?) ?: "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${findProperty("paperVersion") ?: "26.1.2.build.+"}")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("io.lumine:Mythic-Dist:5.12.1")
    // Embarqué et relocalisé dans le jar (objets CraftEngine, ExecutableItems, ItemsAdder, Oraxen, Nexo, MMOItems...)
    implementation("io.github.projectunified:uni-item-all:2.7.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

// Nom du fichier : "Classes and Magie 1.2.3.jar"
val jarName = "Classes and Magie"

tasks.jar {
    archiveFileName.set("$jarName ${project.version}-plain.jar")
}

tasks.shadowJar {
    archiveFileName.set("$jarName ${project.version}.jar")
    dependencies {
        include(dependency("io.github.projectunified:.*"))
    }
    relocate("io.github.projectunified.uniitem", "fr.cykalos.cykclasses.libs.uniitem")
    from("LICENSE")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
