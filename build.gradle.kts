import java.io.OutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.HexFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

kotlin { jvmToolchain(25) }

dependencies {
    implementation("com.openai:openai-java:4.112.0")
    testImplementation("junit:junit:4.13.2")
    intellijPlatform {
        val installed = providers.gradleProperty("localPlatformPath").orNull
        if (installed == null) pycharm("2026.2.1") else local(installed)
        bundledPlugin("Git4Idea")
        bundledPlugin("org.jetbrains.plugins.github")
        bundledModule("intellij.platform.tasks")
        if (!providers.gradleProperty("verificationCli").isPresent) pluginVerifier("1.411")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262.9437"
            untilBuild = "262.*"
        }
    }
    pluginVerification {
        providers.gradleProperty("verificationCli").orNull?.let { cliPath.fileValue(file(it)) }
        ides {
            val releases = mapOf("IU" to "2026.2.2", "WS" to "2026.2.2", "RD" to "2026.2.1", "PY" to "2026.2.1")
            for ((code, release) in releases) {
                val path = providers.gradleProperty("verification${code}Path").orNull
                if (path == null) create(code, release) else local(path)
            }
        }
    }
}

tasks.verifyPlugin {
    providers.gradleProperty("verificationArchive").orNull?.let { archiveFile.fileValue(file(it)) }
    val expectedSha = providers.gradleProperty("verificationSha256")
    inputs.property("expectedSha256", expectedSha.orElse(""))
    doFirst {
        val archive = archiveFile.get().asFile
        val digest = MessageDigest.getInstance("SHA-256")
        DigestInputStream(archive.inputStream(), digest).use { it.transferTo(OutputStream.nullOutputStream()) }
        val actual = HexFormat.of().formatHex(digest.digest())
        logger.lifecycle("Verifying ${archive.name} SHA-256 $actual")
        expectedSha.orNull?.let { require(actual.equals(it, ignoreCase = true)) { "Candidate SHA-256 mismatch" } }
    }
}
