package sh.weller.kqtt.impl

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.future.await
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTClient
import sh.weller.kqtt.api.KQTTMessage

open class KQTTClientImpl : KQTTClient {
    protected lateinit var client: Mqtt3AsyncClient

    override suspend fun connect(connectionParameters: ConnectionParameters) {
        client = Mqtt3Client
            .builder()
            .serverHost(connectionParameters.host)
            .serverPort(connectionParameters.port)
            .buildAsync()
        client.connect().await()
    }

    override suspend fun disconnect() {
        client
            .disconnect()
            .await()
    }

    override suspend fun publish(message: KQTTMessage) {
        client.publishWith()
            .topic(message.topic)
            .payload(message.payload)
            .send()
            .await()
    }
}
