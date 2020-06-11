package sh.weller.kqtt.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
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
