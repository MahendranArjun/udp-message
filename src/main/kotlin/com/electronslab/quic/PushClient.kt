
package com.electronslab.quic

import com.electronslab.pubsub.Message
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import net.luminis.quic.QuicClientConnection
import net.luminis.quic.QuicStream
import net.luminis.quic.log.SysOutLogger
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URI
import java.time.Duration
import java.util.function.Consumer


@OptIn(ExperimentalSerializationApi::class)
class PushClient(private val serverPort: Int) {
    private lateinit var connection: QuicClientConnection
    private var log: SysOutLogger? = null

    fun connect() {
        log = SysOutLogger()
        // log.logPackets(true);     // Set various log categories with log.logABC()
        connection = QuicClientConnection.newBuilder()
            .uri(URI.create("push://localhost:$serverPort"))
            .logger(log)
            .noServerCertificateCheck()
            .build()
        connection.setPeerInitiatedStreamCallback(Consumer { quicStream: QuicStream ->
            Thread { handlePushMessages(quicStream) }
                .start()
        })
        connection.connect(5000, "push")
    }

    private fun handlePushMessages(quicStream: QuicStream) {
        println("Server opens stream.")
        val inputStream = BufferedReader(InputStreamReader(quicStream.inputStream))
        val outputStream = quicStream.outputStream
        try {
            while (true) {
                val line = inputStream.readLine()
                println("Received $line")
                Json.encodeToStream(Message.Subscribe("game"),outputStream)
                outputStream.write("\n".encodeToByteArray())
                Json.encodeToStream(Message.Publish("game", "RRRRRR"),outputStream)
                outputStream.write("\n".encodeToByteArray())
                println("R = ${inputStream.readLine()}")
            }
        } catch (e: Exception) {
            // Done
        }
    }

    private fun shutdown() {
        connection.closeAndWait()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val client = PushClient(8080)
            client.connect()
            val runningTime = Duration.ofMinutes(3)
            Thread.sleep(runningTime.toMillis())
            client.shutdown()
        }
    }
}
