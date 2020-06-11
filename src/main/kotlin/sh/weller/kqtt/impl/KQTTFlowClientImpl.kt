package sh.weller.kqtt.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import sh.weller.kqtt.api.KQTTFlowClient
import sh.weller.kqtt.api.KQTTMessage

class KQTTFlowClientImpl : KQTTClientImpl(), KQTTFlowClient {


    @ExperimentalCoroutinesApi
    override suspend fun subscribe(topic: String): Flow<KQTTMessage> = subscribe(listOf(topic))

    @ExperimentalCoroutinesApi
    override suspend fun subscribe(topics: Collection<String>): Flow<KQTTMessage> = callbackFlow {
        client.subscribeWith()
            .addSubscriptions(topics.toSubscriptions())
            .callback {
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
            }
        }
    }


}