package sh.weller.kqtt

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTCallbackClient
import sh.weller.kqtt.api.KQTTClient
import sh.weller.kqtt.api.KQTTMessage
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class KQTTCallbackClientTest {

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

    private val client = KQTTClient.builder().buildCallbackClient()


    @Test
    fun `Test with Callback`() {
        val numTestMessages = 10
        val workloadDuration = 500L

        val awaitResult = CountDownLatch(numTestMessages)

        runBlocking {
            client.connect(ConnectionParameters("localhost", mqttBroker.getMQTTPort()))

            var receivedResult = false
            val duration = measureTimeMillis {
                client.subscribe("test") {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(workloadDuration)
                        awaitResult.countDown()
                    }
                }

                (1..numTestMessages)
                    .map {
                        launch {
                            client.publish(KQTTMessage("test", "Test $it".toByteArray(Charset.defaultCharset())))
                        }
                    }.joinAll()
                receivedResult = awaitResult.await(10, TimeUnit.SECONDS)
            }
            println("Took $duration")
            Assertions.assertTrue(receivedResult)
        }
    }


}