package sh.weller.kqtt.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import sh.weller.kqtt.api.KQTTCallbackClient
import sh.weller.kqtt.api.KQTTMessage

class KQTTCallbackClientImpl : KQTTClientImpl(), KQTTCallbackClient {

    override suspend fun subscribe(topic: String, callback: (KQTTMessage) -> Unit) = subscribe(listOf(topic), callback)
    override suspend fun subscribe(topics: Collection<String>, callback: (KQTTMessage) -> Unit) {
        client.subscribeWith()
            .addSubscriptions(topics.toSubscriptions())
            .callback {
                CoroutineScope(Dispatchers.IO).launch {
                    logger.debug("Received message on {}", it.topic)
                    callback(it.toKQTTMessage())
                }.start()
            }
            .send()
            .await()
        logger.info("Subscribed to topics {}", topics)
    }

    override suspend fun unsubscribe(topic: String) = unsubscribe(listOf(topic))
    override suspend fun unsubscribe(topics: Collection<String>) {
        client.unsubscribeWith()
            .addTopicFilters(topics.toTopicFilter())
            .send()
            .await()
        logger.info("Unsubscribed from topics {}", topics)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KQTTCallbackClientImpl::class.java)
    }
}
