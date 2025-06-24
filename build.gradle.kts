plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.kapt") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.puntogris.telescope"
version = "0.1.2"

repositories {
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    instrumentCode.set(false)
}

dependencies {
    implementation(project(":clip"))

    // Forked the library and included the native files here because it couldn't find them otherwise
    implementation(files("libs/objectbox-java-4.0.3.jar"))
    implementation("io.objectbox:objectbox-kotlin:4.0.3")
    kapt("io.objectbox:objectbox-processor:4.0.3")

    implementation(files("libs/svgSalamander-1.1.4.jar"))

    implementation("me.xdrop:fuzzywuzzy:1.4.0")

    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
    intellijPlatform {
        // https://plugins.jetbrains.com/docs/intellij/android-studio.html#open-source-plugins-for-android-studio
        // https://plugins.jetbrains.com/docs/intellij/android-studio-releases-list.html
        // https://plugins.jetbrains.com/plugin/22989-android/versions/stable
        bundledPlugin("org.jetbrains.android")

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
