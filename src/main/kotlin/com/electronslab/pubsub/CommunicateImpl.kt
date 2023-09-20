package com.electronslab.pubsub

import com.electronslab.quic.push.udp.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.DatagramSocket
import java.net.InetSocketAddress

class CommunicateImpl : Communicate {

    private var socket:DatagramSocket? = null
    private val subscribers = mutableListOf<Subscriber>()

    override fun join(host: String, port: Int) {
        socket = DatagramSocket(InetSocketAddress(host, port))
    }

    override fun leave() {
        if (socket?.isClosed != true) {
            socket?.close()
        }
    }

    override fun subscribe(topic: String, callback: (String) -> Unit) {

    }

    override fun unsubscribe(topic: String) {
        TODO("Not yet implemented")
    }

    override fun publish(topic: String, message: String) {
        TODO("Not yet implemented")
    }

    private fun sendMessage(message: Message, address: InetSocketAddress) {
        socket?.send(Json.encodeToString(message).encodeToByteArray(), address)
    }


    data class Subscriber(val host: String, val port: Int, val topics:List<String>)
}