plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "com.dualsession"
version = "0.1.0"

kotlin {
    jvmToolchain(21) // Java 17 recommended for long-term support
}

repositories {
    mavenCentral()
}

dependencies {
    /* ---------------------------------------------
     * Kotlin standard library
     * --------------------------------------------- */
    implementation(kotlin("stdlib"))

    /* ---------------------------------------------
     * JSON serialization (session + CSRF record)
     * --------------------------------------------- */
    implementation("com.google.code.gson:gson:2.10.1")

    /* ---------------------------------------------
     * Coroutines (storage abstraction is suspend-based)
     * --------------------------------------------- */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    /* ---------------------------------------------
     * Testing (optional but recommended)
     * --------------------------------------------- */
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.dualsession"
            artifactId = "dual-session-core"
            version = "0.1.0"

            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Ghost-code94/session_management_system")
            credentials {
                username = project.findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_USER")
                    ?: ""
                password = project.findProperty("gpr.key") as String?
                    ?: System.getenv("GITHUB_TOKEN")
                    ?: ""
            }
        }
    }
}
