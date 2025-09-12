import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

group = "dev.samoylenko"
version = "0.1.1-SNAPSHOT"

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
    withSourcesJar(publish = true)

    js {
        nodejs {}
        browser {}
    }

    jvm {}

    sourceSets {
        commonMain.dependencies {
            implementation(libs.konform)
            implementation(libs.kotlin.logging)
            implementation(libs.urlencoder)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.serialization.json)
        }

        jvmTest.dependencies {
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
