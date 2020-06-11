package sh.weller.kqtt

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTClient
import sh.weller.kqtt.api.KQTTMessage
import sh.weller.kqtt.impl.concurrentOnEach
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class KQTTFlowClientTest {

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

    private val client = KQTTClient.builder().buildFlowClient()

    @Test
    fun `Test with Flow`() {
        val numTestMessages = 10
        val workloadDuration = 500L

        val awaitResult = CountDownLatch(numTestMessages)

        runBlocking {
            client.connect(ConnectionParameters("localhost", mqttBroker.getMQTTPort()))
            val subscriptionJob = launch {
                client
                    .subscribe("test")
                    .concurrentOnEach(CoroutineScope(Dispatchers.IO), 10) {
                        delay(workloadDuration)
                        awaitResult.countDown()
                    }
                    .take(numTestMessages)
                    .collect()
            }
            val publishJobs =
                (1..numTestMessages)
                    .map {
                        launch {
                            delay(100)
                            client.publish(KQTTMessage("test", "Test $it".toByteArray(Charset.defaultCharset())))
                        }
                    }

            var receivedResult = false
            val duration = measureTimeMillis {
                publishJobs.plus(subscriptionJob).joinAll()
                receivedResult = awaitResult.await(10, TimeUnit.SECONDS)
            }
            println("Took $duration")
            Assertions.assertTrue(receivedResult)
        }
    }
}