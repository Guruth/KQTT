package sh.weller.kqtt.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.weller.kqtt.api.KQTTCallbackClient
import sh.weller.kqtt.api.KQTTMessage

class KQTTCallbackClientImpl : KQTTClientImpl(), KQTTCallbackClient {

    override suspend fun subscribe(topic: String, callback: (KQTTMessage) -> Unit) = subscribe(listOf(topic), callback)
    override suspend fun subscribe(topics: Collection<String>, callback: (KQTTMessage) -> Unit) {
        withContext(Dispatchers.IO) {
            client.subscribeWith()
                .addSubscriptions(topics.toSubscriptions())
                .callback {
                    CoroutineScope(Dispatchers.IO).launch {
                        callback(it.toKQTTMessage())
                    }.start()
                }
                .send()
                .await()
        }
    }

    override suspend fun unsubscribe(topic: String) = unsubscribe(listOf(topic))
    override suspend fun unsubscribe(topics: Collection<String>) {
        client.unsubscribeWith()
            .addTopicFilters(topics.toTopicFilter())
            .send()
            .await()
    }
}
