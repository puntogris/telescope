plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.kotlin.kapt") version "1.9.20"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.puntogris.telescope"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.apache.xmlgraphics:batik-all:1.18") {
        exclude("xml-apis")
    }

    implementation(project(":clip"))
    implementation(files("libs/svgSalamander-1.1.4.jar"))

    kapt("io.objectbox:objectbox-processor:4.0.3")
    implementation("io.objectbox:objectbox-kotlin:4.0.3")
    implementation("io.objectbox:objectbox-macos:4.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

}
// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.1.5")
    type.set("IC")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
