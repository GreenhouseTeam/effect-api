import dev.greenhouseteam.effectapi.gradle.Properties
import dev.greenhouseteam.effectapi.gradle.Versions
import org.apache.tools.ant.filters.LineContains

plugins {
    id("effectapi.loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = Versions.NEOFORGE

    val at = project(":base:base-common").file("src/main/resources/${Properties.MOD_ID}_base.cfg")
    if (at.exists())
        accessTransformers.add(at.absolutePath)

    mods {
        register("${Properties.MOD_ID}_base") {
            sourceSet(sourceSets["main"])
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        filesMatching("*.mixins.json") {
            filter<LineContains>("negate" to true, "contains" to setOf("refmap"))
        }
    }
}