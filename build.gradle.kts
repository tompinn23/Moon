
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.shadowJar {
    relocate("com.electronwill", "org.yonside.moon.coremod.shaded.electronwill")
}
