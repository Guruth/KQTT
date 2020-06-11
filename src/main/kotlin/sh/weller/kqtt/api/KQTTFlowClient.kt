package sh.weller.kqtt.api

import kotlinx.coroutines.flow.Flow

/**
 * KQTTClient offering a Flow based interface
 * @see Flow
 * @see KQTTClient
 */
interface KQTTFlowClient : KQTTClient {

    /**
     * Subscribes to the collection of topics.
     * Unsubscribes automatically when the flow is closed.
     *
     * @param topics the topics to subscribe to
     * @return a flow of received messages
     * @see KQTTMessage
     */
    suspend fun subscribe(topics: Collection<String>): Flow<KQTTMessage>

    /**
     * Subscribes to the given topic.
     * Unsubscribes automatically when the flow is closed.
     *
     * @param topic the topic to subscribe to
     * @return a flow of received messages
     * @see KQTTMessage
     */
    suspend fun subscribe(topic: String): Flow<KQTTMessage>
}
