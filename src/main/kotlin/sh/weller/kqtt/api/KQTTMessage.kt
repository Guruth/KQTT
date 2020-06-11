package sh.weller.kqtt.api


/**
 * The payload and topic of received and sent messages.
 *
 * @property topic the topic of on which messages are sent or received
 * @property payload the payload of the message
 */
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