package com.electronslab.pubsub

data class Subscriber(val host:String, val port:Int, val topics:List<String>)