plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
kotlin {
    jvm("desktop")
    jvmToolchain(21)
    sourceSets {
        commonMain.dependencies { implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0") }
        commonTest.dependencies { implementation(kotlin("test")) }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
            }
        }
        val desktopTest by getting { dependencies { implementation(kotlin("test-junit")) } }
    }
}
compose.desktop {
    application {
        mainClass = "bandlogger.MainKt"
        nativeDistributions {
            packageName = "band.logger"
            packageVersion = "0.5.0"
            description = "A workspace for musicians and working bands"
            vendor = "band.logger"
            modules("java.sql", "java.xml", "jdk.unsupported")
        }
    }
}
