package com.electronslab.udp.message

data class Message(val id:String, val type:Type, val data:String?) {
    enum class Type { REQ, RESP, ACK }
}