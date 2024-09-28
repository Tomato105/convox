plugins {
    kotlin("jvm") version "2.0.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
}


group = "org.shinytomato"
version = "1.0-SNAPSHOT"

javafx {
    version = "22.0.2"
    modules("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    testImplementation(kotlin("test"))
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    manifest {
        attributes["Main-Class"] = "org.shinytomato.convox.ConvoxApplicationKt"
    }
}

kotlin {
    jvmToolchain(17)
}
