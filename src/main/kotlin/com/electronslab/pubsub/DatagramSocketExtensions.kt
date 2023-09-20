package com.electronslab.quic.push.udp


import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress


fun DatagramSocket.send(data: ByteArray, address: InetSocketAddress) {
    val packet = DatagramPacket(data, data.size, address)
    send(packet)
}

fun DatagramSocket.sendText(text: String, address: InetSocketAddress) {
    send(text.encodeToByteArray(), address)
}

fun DatagramSocket.receive(): Pair<ByteArray, InetSocketAddress> {
    val buffer = ByteArray(65527)
    val packet = DatagramPacket(buffer, buffer.size)
    receive(packet)
    return packet.data.copyOf(packet.length) to InetSocketAddress(packet.address.hostName, packet.port)
}

fun DatagramSocket.receiveText(): Pair<String, InetSocketAddress> {
    return receive().let { it.first.decodeToString() to it.second }
}

inline fun DatagramSocket.onMessage(onReceived:(ByteArray, InetSocketAddress)->Unit) {
    while (!isClosed) {
        val (data, address) = receive()
        onReceived(data, address)
    }
}