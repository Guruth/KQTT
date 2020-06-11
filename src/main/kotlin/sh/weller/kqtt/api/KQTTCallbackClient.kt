package sh.weller.kqtt.api

/**
 * KQTTClient offering a callback based interface.
 * @see KQTTClient
 */
interface KQTTCallbackClient : KQTTClient {

    /**
     * Subscribes to the collection of topics.
     * The callback is launched in a coroutine with Dispatchers.IO
     *
     * Does not unsubscribe automatically!
     *
     * @param topics the topics to subscribe to
     * @param callback the callback receiving the message
     *
     * @see KQTTMessage
     */
    suspend fun subscribe(topics: Collection<String>, callback: (KQTTMessage) -> Unit)

    /**
     * Subscribes to the topic.
     * The callback is launched in a coroutine with Dispatchers.IO
     *
     * Does not unsubscribe automatically!
     *
     * @param topic the topic to subscribe to
     * @param callback the callback receiving the message
     *
     * @see KQTTMessage
     */
    suspend fun subscribe(topic: String, callback: (KQTTMessage) -> Unit)

    /**
     * Unsubscribes from the given topic.
     * @param topic the topic to unsubscribe from.
     */
    suspend fun unsubscribe(topic: String)

    /**
     * Unsubscribes fro the given topics.
     * @param topics the collection of topics to unsubscribe from.
     */
    suspend fun unsubscribe(topics: Collection<String>)

}