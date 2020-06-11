package sh.weller.kqtt

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTMessage
import sh.weller.kqtt.impl.KQTTFlowClientImpl
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class KQTTFlowClientTest {

    val numTestMessages = 10
    val workloadDuration = 500L

    @Test
    fun `Test with Flow`() {
        val awaitResult = CountDownLatch(numTestMessages)

        runBlocking {
            val client = KQTTFlowClientImpl()
            client.connect(ConnectionParameters("localhost", 1883))
            val subscriptionJob = launch {
                client
                    .subscribe("test")
                    .concurrentMap(CoroutineScope(Dispatchers.IO), 10) {
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

    private fun <T, R> Flow<T>.concurrentMap(
        scope: CoroutineScope,
        concurrencyLevel: Int,
        transform: suspend (T) -> R
    ): Flow<R> =
        this
            .map { scope.async { transform(it) } }
            .buffer(concurrencyLevel)
            .map { it.await() }
}