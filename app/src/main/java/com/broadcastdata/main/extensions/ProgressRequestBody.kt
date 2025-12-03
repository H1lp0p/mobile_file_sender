package com.broadcastdata.main.extensions

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream

class ProgressRequestBody(
    private val inputStream: InputStream,
    private val contentLength: Long,
    private val fileName: String,
    private val mediaType: MediaType = "application/octet-stream".toMediaType(),
    private val onProgress: (percentage: Int) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = mediaType

    override fun contentLength(): Long = contentLength

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded: Long = 0

        try {
            inputStream.use { input ->
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    uploaded += read.toLong()
                    sink.write(buffer, 0, read)

                    // Обновляем прогресс
                    val progress = (uploaded * 100 / contentLength).toInt()
                    onProgress(progress)
                }
            }
        } finally {
            inputStream.close()
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}