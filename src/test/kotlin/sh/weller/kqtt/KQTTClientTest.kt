package sh.weller.kqtt

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.*
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTMessage
import sh.weller.kqtt.impl.KQTTClientImpl
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class KQTTClientTest {

    val numTestMessages = 100
    val workloadDuration = 5000L
    val isLog = false

    @Test
    fun `Test with Flow`() {
        val awaitResult = CountDownLatch(numTestMessages)

        runBlocking {
            val client = KQTTClientImpl()
            client.connect(ConnectionParameters("localhost", 1883))
            val subscriptionJob = launch {
                client
                    .subscribe("test")
                    .concurrentMap(CoroutineScope(Dispatchers.IO), 100) {
                        delay(workloadDuration)
                        log(it.payload?.toString(Charset.defaultCharset()))
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


    @Test
    fun `Test with Callback`() {
        val awaitResult = CountDownLatch(numTestMessages)

        runBlocking {
            val client = KQTTClientImpl()
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

fun <T, R> Flow<T>.concurrentMap(scope: CoroutineScope, concurrencyLevel: Int, transform: suspend (T) -> R): Flow<R> =
    this
        .map { scope.async { transform(it) } }
        .buffer(concurrencyLevel)
        .map { it.await() }