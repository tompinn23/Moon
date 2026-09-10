
pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroup("com.gtnewhorizons")
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
            }
        }
        maven {
            name = "Yonside"
            url = uri("https://maven.yonside.org")
            mavenContent {
                includeGroup("org.yonside")
                includeGroupByRegex("org\\.yonside\\..+")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("com.gtnewhorizons.gtnhsettingsconvention") version("2.0.20")
    id("org.yonside.plink.settings") version("0.1.0-2-g144145c")
}

plink {
    catalogs {
        create("gtnh") {
            version = "2.9.0-beta-3"
        }
    }
}
