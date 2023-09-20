package com.electronslab.pubsub

import com.electronslab.quic.push.udp.receiveText
import com.electronslab.quic.push.udp.send
import com.electronslab.quic.push.udp.sendText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress

class PubSubServer(
    host:String,
    port:Int,
):Closeable {

    private val socket = DatagramSocket(InetSocketAddress(host, port))
    private val subscribers = mutableListOf<Subscriber>()

    init {
        while (!socket.isClosed) {
            val (message, address) = receiveMessage()
            onReceiveMessage(message, address)
        }
    }

    private fun onReceiveMessage(message: Message, address: InetSocketAddress){
        when(message){
            is Message.Subscribe -> onSubscribe(message, address)
            is Message.Unsubscribe -> onUnsubscribe(message, address)
            is Message.Publish -> TODO()
        }
    }

    private fun onSubscribe(message: Message.Subscribe, address: InetSocketAddress){

    }

    private fun onUnsubscribe(message: Message.Unsubscribe, address: InetSocketAddress){

    }

    override fun close() {
        if (!socket.isClosed) {
            socket.close()
        }
    }

    private fun sendMessage(message: Message, address: InetSocketAddress) {
        socket.sendText(Json.encodeToString(message), address)

    }

    private fun receiveMessage(): Pair<Message, InetSocketAddress> {
        return socket.receiveText().let { Json.decodeFromString<Message>(it.first) to it.second }
    }

}