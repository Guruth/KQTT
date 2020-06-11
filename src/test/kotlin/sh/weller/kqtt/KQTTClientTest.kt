package sh.weller.kqtt

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTClient

class KQTTClientTest {

    companion object {

        private val mqttBroker: KGenericContainer = mqttBroker()

        @BeforeAll
        @JvmStatic
        fun setup() {
            mqttBroker.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            mqttBroker.stop()
        }
    }


    @Test
    fun `Client connects and disconnects`() {
        runBlocking {
            val client = KQTTClient.builder().buildCallbackClient()

            client.connect(ConnectionParameters("localhost", mqttBroker.getMQTTPort()))
            Assertions.assertTrue(client.isConnected())

            client.disconnect()
            Assertions.assertFalse(client.isConnected())


            client.connect(ConnectionParameters("localhost", mqttBroker.getMQTTPort()))
            Assertions.assertTrue(client.isConnected())
        }
    }

}