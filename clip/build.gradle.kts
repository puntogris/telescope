plugins {
    id("java")
    id("application")
    id("cpp")
    kotlin("jvm")
}

group = "android.clip.cpp"

repositories {
    mavenCentral()
}

application {
    mainClass.set("CLIPAndroid") // `mainClassName` becomes `mainClass` in Kotlin DSL
    applicationDefaultJvmArgs = listOf(
        "-Djava.library.path=${file("${buildDir}/libs/shared/clip").absolutePath}"
    )
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}