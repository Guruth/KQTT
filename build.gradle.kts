plugins {
    idea
    `java-library`
    `maven-publish`

    kotlin("jvm") version "1.3.72"

    id("org.jetbrains.dokka") version "0.10.1"
    id("io.gitlab.arturbosch.detekt").version("1.9.1")
}

group = "sh.weller"
version = "1.0-SNAPSHOT"

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}


repositories {
    mavenCentral()
    jcenter()
}

val coroutinesVersion= "1.3.7"
val testContainersVersion = "1.14.3"
val slf4jVersion = "1.7.30"

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")

    implementation("com.hivemq:hivemq-mqtt-client:1.2.0")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    testImplementation ("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        useJUnitPlatform()
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "$buildDir/dokka"
    }
}

detekt {
    reports {
        html {
            enabled = true
        }
    }
}
