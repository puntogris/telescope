plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.kapt") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("io.objectbox") version "4.0.3"
}

group = "com.puntogris.telescope"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.apache.xmlgraphics:batik-all:1.18") {
        exclude("xml-apis")
    }

    implementation("xml-apis:xml-apis-ext:1.3.04")

    implementation(project(":clip"))

    implementation(files("libs/svgSalamander-1.1.4.jar"))

    kapt("io.objectbox:objectbox-processor:4.0.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    intellijPlatform {
        // https://plugins.jetbrains.com/docs/intellij/android-studio.html#open-source-plugins-for-android-studio
        // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
        // https://plugins.jetbrains.com/plugin/22989-android/versions/stable
        bundledPlugin("org.jetbrains.android")
        instrumentationTools()

        // TODO maybe is a workspace error but having trouble using ide.localPath on AS, plugin path seems broken
        if (hasProperty("ide.localPath")) {
            local(properties["ide.localPath"].toString())
        } else if (hasProperty("ide.version")) {
            androidStudio(properties["ide.version"].toString())
        }
    }
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
        sinceBuild.set("242.23339")
        untilBuild.set("243.*")
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
