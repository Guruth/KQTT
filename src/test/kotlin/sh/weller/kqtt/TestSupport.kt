package sh.weller.kqtt

import org.testcontainers.containers.GenericContainer

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

fun mqttBroker(): KGenericContainer = KGenericContainer("eclipse-mosquitto")
    .withExposedPorts(1883)

fun KGenericContainer.getMQTTPort(): Int =
    getMappedPort(1883)