package com.electronslab.udp.message

import java.net.InetSocketAddress

interface MessageServer {
    suspend fun onRequest(message: Message, address: InetSocketAddress) {
        sendMessage(message.copy(type = Message.Type.ACK), address)
    }

    suspend fun onResponse(message: Message, address: InetSocketAddress){

    }
    suspend fun onAcknowledgement(message: Message, address: InetSocketAddress)

    suspend fun sendMessage(message: Message, address: InetSocketAddress)
    suspend fun receiveMessage(): Pair<Message, InetSocketAddress>

    suspend fun response(data: String, address: InetSocketAddress): String
}