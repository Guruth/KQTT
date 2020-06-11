package sh.weller.kqtt.impl

import com.hivemq.client.mqtt.datatypes.MqttTopicFilter
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription
import sh.weller.kqtt.api.KQTTMessage

internal fun Collection<String>.toSubscriptions() =
    map { Mqtt3Subscription.builder().topicFilter(it).build() }

internal fun Collection<String>.toTopicFilter() =
    map { MqttTopicFilter.of(it) }


internal fun Mqtt3Publish.toKQTTMessage() = KQTTMessage(topic.toString(), payloadAsBytes)
