@file:OptIn(ExperimentalSerializationApi::class)

package com.electronslab.quic

import com.electronslab.pubsub.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.luminis.quic.QuicStream
import net.luminis.quic.Version
import net.luminis.quic.log.Logger
import net.luminis.quic.log.SysOutLogger
import net.luminis.quic.server.ApplicationProtocolConnection
import net.luminis.quic.server.ServerConnector
import java.io.FileInputStream
import kotlin.coroutines.CoroutineContext

class PushServer private constructor(port:Int, certFile:String, keyFile:String):CoroutineScope {


    private val messagesFlow = MutableSharedFlow<Message.Publish>()

    private val logger = SysOutLogger().apply {
        timeFormat(Logger.TimeFormat.Long)
        logWarning(true)
        logInfo(true)
    }

    private val connector = ServerConnector(
        port,
        FileInputStream(certFile),
        FileInputStream(keyFile),
        listOf(Version.QUIC_version_1),
        false,
        logger
    )

    init {
        connector.registerApplicationProtocol("push") { _, connection ->
            val stream = connection.createStream(true)
            launch {
                stream.outputStream.write("\n".encodeToByteArray())
                var job:Job ? = null
                while (true) {
                    val message = Json.decodeFromString<Message>(stream.inputStream.bufferedReader().readLine())
                    when (message) {
                        is Message.Subscribe -> {
                            job = messagesFlow
                                .onEach {
                                    if (it.topic == message.topic) {
                                        stream.outputStream.write(it.message.encodeToByteArray())
                                        stream.outputStream.write("\n".encodeToByteArray())
                                    }
                                }
                                .flowOn(Dispatchers.IO)
                                .launchIn(this@PushServer)
                        }
                        is Message.Unsubscribe -> {
                            job?.cancel()
                        }
                        is Message.Publish -> messagesFlow.emit(message)
                    }
                }
            }
            object : ApplicationProtocolConnection {}
        }

        connector.start()
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PushServer(8080, "ca-cert.pem", "ca-key.pem")
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()


    private suspend fun onConnected(stream: QuicStream) = withContext(Dispatchers.IO) {
        val outputStream = stream.outputStream
        val inputStream = stream.inputStream
        outputStream.write("\n".encodeToByteArray())
        while (isActive) {
            delay(1000)
            println(Json.decodeFromString<Message.Subscribe>(inputStream.bufferedReader().readLine()))
        }
    }
}
