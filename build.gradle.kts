@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

group = "dev.samoylenko"
version = "0.2.1-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "Central Snapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

plugins {
    signing

    alias(libs.plugins.kotlin.plugin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApi()

    withSourcesJar(publish = true)

    jvm()

    js {
        browser()
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
    }

    wasmWasi {
        nodejs()
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.konform)
            implementation(libs.kotlin.logging)
            implementation(libs.urlencoder)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))

            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.client.cio) // https://ktor.io/docs/client-engines.html#limitations
            implementation(libs.logback.classic)
        }
    }
}

signing {
    useGpgCmd()
}

mavenPublishing {
    signAllPublications()
    publishToMavenCentral()

    pom {
        name = "PackageURL Basic - Kotlin Multiplatform"
        description = "A very basic library for working with PackageURL / PURL (Kotlin Multiplatform)"
        url = "https://github.com/samoylenko/packageurl-basic"

        licenses {
            license {
                name = "The MIT License"
                url = "https://github.com/samoylenko/packageurl-basic/blob/main/LICENSE"
            }
        }

        developers {
            developer {
                id = "samoylenko"
                name = "Michael Samoylenko"
                url = "https://github.com/samoylenko"
            }
        }

        scm {
            url = "https://github.com/samoylenko/packageurl-basic.git"
            connection = "scm:git:git@github.com:samoylenko/packageurl-basic.git"
            developerConnection = "scm:git:ssh://git@github.com:samoylenko/packageurl-basic.git"
        }
    }
}

rootProject.plugins.withType(YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().ignoreScripts = true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport = YarnLockMismatchReport.WARNING
}
