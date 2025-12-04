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
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.dualsession:dual-session-core:0.1.0")

    implementation(kotlin("stdlib"))

    // Ktor 1.x server core – match this to your existing version if different
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
}
