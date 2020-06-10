plugins {
    kotlin("jvm") version "1.3.72"
    idea
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

group = "sh.weller"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.7")

    implementation("com.hivemq:hivemq-mqtt-client:1.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.7")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin{
        kotlinOptions.jvmTarget = "1.8"
    }
    test{
        useJUnitPlatform()
    }
}
