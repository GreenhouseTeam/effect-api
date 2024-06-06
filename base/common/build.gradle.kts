import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions

plugins {
    id("effectapi.common")
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

sourceSets {
    create("generated") {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

minecraft {
    version(Versions.INTERNAL_MINECRAFT)
    val aw = file("src/main/resources/${Properties.MOD_ID}-base.accesswidener")
    if (aw.exists())
        accessWideners(aw)
}

dependencies {
    compileOnly("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${Versions.MIXIN_EXTRAS}")
    compileOnly("net.fabricmc:sponge-mixin:${Versions.FABRIC_MIXIN}")
}

configurations {
    register("baseCommonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("baseCommonTestJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("baseCommonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("baseCommonTestResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("baseCommonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("baseCommonTestJava", sourceSets["test"].java.sourceDirectories.singleFile)
    add("baseCommonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
    add("baseCommonTestResources", sourceSets["test"].resources.sourceDirectories.singleFile)
}