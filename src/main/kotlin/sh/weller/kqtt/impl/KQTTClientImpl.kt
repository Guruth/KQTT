package sh.weller.kqtt.impl

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.future.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sh.weller.kqtt.api.ConnectionParameters
import sh.weller.kqtt.api.KQTTClient
import sh.weller.kqtt.api.KQTTMessage

abstract class KQTTClientImpl : KQTTClient {
    protected lateinit var client: Mqtt3AsyncClient

    override suspend fun connect(connectionParameters: ConnectionParameters) {
        client = Mqtt3Client
            .builder()
            .serverHost(connectionParameters.host)
            .serverPort(connectionParameters.port)
            .buildAsync()
        client.connect().await()
        logger.info("Connected to broker {}:{}", connectionParameters.host, connectionParameters.port)
    }

    override fun isConnected(): Boolean = client.state.isConnected

    override suspend fun disconnect() {
        client
            .disconnect()
            .await()
        logger.info("Disconnected from broker.")
    }

    override suspend fun publish(message: KQTTMessage) {
        client.publishWith()
            .topic(message.topic)
            .payload(message.payload)
            .send()
            .await()
        logger.debug("Published message to {}", message.topic)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(KQTTClientImpl::class.java)
    }
}
