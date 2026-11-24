plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

kotlin { jvmToolchain(25) }

dependencies {
    testImplementation("junit:junit:4.13.2")
    intellijPlatform {
        val installed = providers.gradleProperty("localPlatformPath").orNull
        if (installed == null) pycharm("2026.2.1") else local(installed)
        bundledPlugin("Git4Idea")
        bundledPlugin("org.jetbrains.plugins.github")
        bundledModule("intellij.platform.tasks")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262.9437"
            untilBuild = "262.*"
        }
    }
}
