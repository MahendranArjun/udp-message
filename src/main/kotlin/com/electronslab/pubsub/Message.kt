package com.electronslab.pubsub

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Polymorphic
sealed class Message(private val type:Type) {

    @Serializable
    @SerialName("Subscribe")
    data class Subscribe(val topic: String) : Message(Type.Subscribe)

    @Serializable
    @SerialName("Unsubscribe")
    data class Unsubscribe(val topic: String) : Message(Type.Unsubscribe)

    @Serializable
    @SerialName("Publish")
    data class Publish(val topic: String, val message:String) : Message(Type.Publish)

    enum class Type { Subscribe, Unsubscribe, Publish }
}
