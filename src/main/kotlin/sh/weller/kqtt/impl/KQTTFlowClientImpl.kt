package sh.weller.kqtt.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import sh.weller.kqtt.api.KQTTFlowClient
import sh.weller.kqtt.api.KQTTMessage

@ExperimentalCoroutinesApi
class KQTTFlowClientImpl : KQTTClientImpl(), KQTTFlowClient {


    override suspend fun subscribe(topic: String): Flow<KQTTMessage> = subscribe(listOf(topic))

    override suspend fun subscribe(topics: Collection<String>): Flow<KQTTMessage> = callbackFlow {
        client.subscribeWith()
            .addSubscriptions(topics.toSubscriptions())
            .callback {
                logger.debug("Received message on topic {}", it.topic)
                sendBlocking(it.toKQTTMessage())
            }
            .send()
            .await()

        awaitClose {
            runBlocking {
                client.unsubscribeWith()
                    .addTopicFilters(topics.toTopicFilter())
                    .send()
                    .await()
                logger.info("Unsubscribed from topics {}", topics)
            }
        }
    }


    companion object {
        private val logger = LoggerFactory.getLogger(KQTTFlowClientImpl::class.java)
    }
}

/**
 * Work around for consuming a flow concurrently.
 * Taken from https://github.com/Kotlin/kotlinx.coroutines/issues/1147#issuecomment-639397185
 *
 * @param scope coroutine scope the work should be done
 * @param concurrencyLevel number of workloads that should be started
 * @param transform the mapping function
 *
 * @return the mapped flow
 */
fun <T, R> Flow<T>.concurrentMap(
    scope: CoroutineScope,
    concurrencyLevel: Int,
    transform: suspend (T) -> R
): Flow<R> =
    this
        .map { scope.async { transform(it) } }
        .buffer(concurrencyLevel)
        .map { it.await() }

/**
 * Work around for consuming a flow concurrently.
 * Taken from https://github.com/Kotlin/kotlinx.coroutines/issues/1147#issuecomment-639397185
 *
 * @param scope coroutine scope the work should be done
 * @param concurrencyLevel number of workloads that should be started
 * @param transform the function to execute
 *
 * @return the flow
 */
fun <T> Flow<T>.concurrentOnEach(
    scope: CoroutineScope,
    concurrencyLevel: Int,
    transform: suspend (T) -> Unit
): Flow<T> =
    this
        .map { Pair(scope.launch { transform(it) }, it) }
        .buffer(concurrencyLevel)
        .map {
            it.first.start()
            return@map it.second
        }