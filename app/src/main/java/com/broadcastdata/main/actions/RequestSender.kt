package com.broadcastdata.main.actions

import com.broadcastdata.main.extensions.ProgressRequestBody
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import javax.inject.Inject

class RequestSender @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    fun check(host: String, port: Int): Response {
        val url = "http://$host:$port/check"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()

        return response
    }

    fun sendFile(host: String, port: Int, file: InputStream, fileName: String, fileSize: Long, dirName: String, onProgress: (percentage: Int) -> Unit): Response{
        val url = "http://$host:$port/upload"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                fileName,
                ProgressRequestBody(file, fileSize, fileName) { progress ->
                    onProgress(progress)
                }
            )
            .addFormDataPart("timestamp", System.currentTimeMillis().toString())
            .addFormDataPart("dir_name", dirName)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()

        return response
    }

    companion object{
        private const val TAG = "RequestSender"
    }
}