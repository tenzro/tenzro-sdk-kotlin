plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    `maven-publish`
}

group = "network.tenzro"
version = "0.1.0"

repositories { mavenCentral() }

dependencies {
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin { jvmToolchain(17) }

java {
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
    // Forward the optional live-node settings to the test JVM.
    listOf("TENZRO_RPC_URL", "TENZRO_CHAIN_ID").forEach { k ->
        System.getenv(k)?.let { environment(k, it) }
    }
    testLogging { events("passed", "skipped", "failed"); showStandardStreams = true }
}

publishing {
    publications {
        create<MavenPublication>("maven") { from(components["java"]) }
    }
}
