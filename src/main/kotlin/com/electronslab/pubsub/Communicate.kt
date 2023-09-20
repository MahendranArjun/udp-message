package com.electronslab.pubsub



interface Communicate {

    fun join(host: String, port: Int)

    fun leave()

    fun subscribe(topic: String, callback: (String) -> Unit)

    fun unsubscribe(topic: String)

    fun publish(topic: String, message: String)


}