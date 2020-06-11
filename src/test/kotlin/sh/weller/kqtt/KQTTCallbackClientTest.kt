package sh.weller.kqtt

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.*
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTClient
import sh.weller.kqtt.api.KQTTMessage
import sh.weller.kqtt.impl.KQTTCallbackClientImpl
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class KQTTCallbackClientTest {

    val numTestMessages = 10
    val workloadDuration = 500L
    val isLog = false

    @Test
    fun `Test with Callback`() {
        val awaitResult = CountDownLatch(numTestMessages)

        runBlocking {
            val client = KQTTClient.builder().buildCallbackClient()
            client.connect(ConnectionParameters("localhost", 1883))

            var receivedResult = false
            val duration = measureTimeMillis {
                client.subscribe("test") {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(workloadDuration)
                        log(it.payload?.toString(Charset.defaultCharset()))
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

    private fun log(msg: String?) {
        if (isLog) {
            println("[${Thread.currentThread().name}] $msg")
        }
    }
}