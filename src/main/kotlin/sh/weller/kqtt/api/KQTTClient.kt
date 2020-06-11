package sh.weller.kqtt.api

import sh.weller.kqtt.impl.KQTTCallbackClientImpl
import sh.weller.kqtt.impl.KQTTFlowClientImpl

/**
 * Wrapper around the HiveMQTT3Client offering suspending functions.
 *
 * @see KQTTFlowClient
 * @see KQTTCallbackClient
 */
interface KQTTClient {

    /**
     * Opens the connection.
     *
     * @param connectionParameters The connection parameters.
     * @see ConnectionParameters
     */
    suspend fun connect(connectionParameters: ConnectionParameters)

    /**
     * Closes the connection.
     */
    suspend fun disconnect()

    /**
     * Publishes the given message.
     *
     * @param message the message to be published
     * @see KQTTMessage
     */
    suspend fun publish(message: KQTTMessage)

    class Builder {
        fun buildFlowClient(): KQTTFlowClient = KQTTFlowClientImpl()

        fun buildCallbackClient(): KQTTCallbackClient = KQTTCallbackClientImpl()
    }

    companion object {
        fun builder() = Builder()
    }
}

/**
 * Connection Parameters
 *
 * @property host the host name of the mqtt server
 * @property port the port of the mqtt server
 */
data class ConnectionParameters(
    val host: String,
    val port: Int
)

