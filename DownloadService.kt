package com.elab.download

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File


object DownloadService {


    fun download(url: String, destination: File): Flow<State> = flow {
        val httpClient = OkHttpClient.Builder()
            .build()
        val request = Request.Builder()
            .get()
            .url(url)
            .build()
        val states = httpClient.newCall(request)
            .execute()
            .download(url, destination)
        emitAll(states)
    }

    private fun Response.download(url: String, destination: File): Flow<State> {
        return flow {
            if (isSuccessful && body != null && code == 200) {
                val totalSize = body!!.contentLength()
                body!!.byteStream().use { input ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                        var downloadedSize = 0.0
                        while (currentCoroutineContext().isActive) {
                            val readSize = input.read(buffer)
                            if (readSize == -1) break
                            output.write(buffer, 0, readSize)
                            downloadedSize += readSize
                            val progress = (downloadedSize / totalSize) * 100
                            emit(State.Progress(url, progress))
                        }
                    }
                }
                emit(State.Completed(url))
            } else {
                emit(State.Failure(url, code, message))
            }
        }
    }


    sealed interface State {
        val downloadUrl: String

        data class Completed(override val downloadUrl: String) : State
        data class Progress(override val downloadUrl: String, val progress: Double) : State
        data class Failure(override val downloadUrl: String, val code: Int, val message: String?) : State
    }

    private const val DOWNLOAD_BUFFER_SIZE = 1024 * 8

}