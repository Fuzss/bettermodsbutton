plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    gradlePluginPortal()
//    maven("https://maven.architectury.dev/") // Add this line
    maven { url = uri("https://maven.architectury.dev/") }
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.neoforged.net/releases/") }
    maven { url = uri("https://maven.minecraftforge.net/") }
}

dependencies {
    // Add the Architectury Loom plugin as a buildscript dependency so you can import its classes
    implementation("dev.architectury:architectury-loom:1.11-SNAPSHOT")
    implementation("architectury-plugin:architectury-plugin.gradle.plugin:3.4-SNAPSHOT")

//    // So you can import Loom's plugin + API in your code
//    compileOnly("dev.architectury:architectury-loom:1.11-SNAPSHOT")
//
//    // If you want to unit-test plugin logic, put Loom on the test runtime too
//    testImplementation("dev.architectury:architectury-loom:1.11-SNAPSHOT")
}

gradlePlugin {
    plugins {
        register("commonConventions") {
            id = "fuzs.multiloader.common-conventions"
            implementationClass = "fuzs.multiloader.CommonConventionsPlugin"
        }
    }
}
