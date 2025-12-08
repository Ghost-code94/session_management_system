plugins {
    kotlin("jvm") version "1.9.23"
    `maven-publish`
}

group = "com.dualsession"
version = "0.1.0"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    mavenLocal()   // optional, but nice for local dev
}

dependencies {
    implementation("com.dualsession:dual-session-core:0.1.0")

    implementation(kotlin("stdlib"))

    // Ktor 1.x server core – match ghostcache API’s version
    implementation("io.ktor:ktor-server-core:1.6.8")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.dualsession"
            artifactId = "dual-session-ktor"
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
