package sh.weller.kqtt.api

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface KQTTClient {
    suspend fun connect(connectionParameters: ConnectionParameters)
    suspend fun disconnect()

    suspend fun subscribe(topics: Collection<String>): Flow<KQTTMessage>
    suspend fun subscribe(topic: String): Flow<KQTTMessage>
    suspend fun subscribe(topics: Collection<String>, callback: (KQTTMessage) -> Unit)
    suspend fun subscribe(topic: String, callback: (KQTTMessage) -> Unit)

    suspend fun unsubscribe(topic: String)
    suspend fun unsubscribe(topics: Collection<String>)

    suspend fun publish(message: KQTTMessage)
}

data class ConnectionParameters(
    val host: String,
    val port: Int
)

data class KQTTMessage(
    val topic: String,
    val payload: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KQTTMessage

        if (topic != other.topic) return false
        if (payload != null) {
            if (other.payload == null) return false
            if (!payload.contentEquals(other.payload)) return false
        } else if (other.payload != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topic.hashCode()
        result = 31 * result + (payload?.contentHashCode() ?: 0)
        return result
    }
}