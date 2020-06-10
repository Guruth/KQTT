package sh.weller.kqtt.impl

import com.hivemq.client.mqtt.datatypes.MqttTopicFilter
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.future.await
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTClient
import sh.weller.kqtt.api.KQTTMessage

class KQTTClientImpl : KQTTClient {
    lateinit var client: Mqtt3AsyncClient
    val isLog = false


    override suspend fun connect(connectionParameters: ConnectionParameters) {
        client = Mqtt3Client
            .builder()
            .serverHost(connectionParameters.host)
            .serverPort(connectionParameters.port)
            .buildAsync()
        client.connect().await()
        log("Connected")
    }

    override suspend fun disconnect() {
        client
            .disconnect()
            .await()
        log("Disconnected")
    }

    @ExperimentalCoroutinesApi
    override suspend fun subscribe(topic: String): Flow<KQTTMessage> = subscribe(listOf(topic))

    @ExperimentalCoroutinesApi
    override suspend fun subscribe(topics: Collection<String>): Flow<KQTTMessage> = callbackFlow {
        client.subscribeWith()
            .addSubscriptions(topics.toSubscriptions())
            .callback {
                log("Received on topic ${it.topic}")
                sendBlocking(it.toKQTTMessage())
            }
            .send()
            .await()
        log("Subscribed")

        awaitClose {
            runBlocking {
                client.unsubscribeWith()
                    .addTopicFilters(topics.toTopicFilter())
                    .send()
                    .await()
                log("Unsubscribed")
            }
        }
    }


    override suspend fun subscribe(topic: String, callback: (KQTTMessage) -> Unit) = subscribe(listOf(topic), callback)
    override suspend fun subscribe(topics: Collection<String>, callback: (KQTTMessage) -> Unit) {
        withContext(Dispatchers.IO) {
            client.subscribeWith()
                .addSubscriptions(topics.toSubscriptions())
                .callback {
                    CoroutineScope(Dispatchers.IO).launch {
                        log("Received on topic ${it.topic}")
                        callback(it.toKQTTMessage())
                    }.start()
                }
                .send()
                .await()
            log("Subscribed")
        }
    }


    override suspend fun unsubscribe(topic: String) {
        TODO("Not yet implemented")
    }

    override suspend fun unsubscribe(topics: Collection<String>) {
        TODO("Not yet implemented")
    }

    override suspend fun publish(message: KQTTMessage) {
        withContext(Dispatchers.IO) {
            client.publishWith()
                .topic(message.topic)
                .payload(message.payload)
                .send()
                .await()
            log("Published")
        }
    }

    private fun Collection<String>.toSubscriptions() =
        map { Mqtt3Subscription.builder().topicFilter(it).build() }

    private fun Collection<String>.toTopicFilter() =
        map { MqttTopicFilter.of(it) }


    private fun Mqtt3Publish.toKQTTMessage() = KQTTMessage(topic.toString(), payloadAsBytes)

    private fun log(msg: String) =
        if (isLog) {
            println("[${Thread.currentThread().name}] $msg")
        } else {
        }
}