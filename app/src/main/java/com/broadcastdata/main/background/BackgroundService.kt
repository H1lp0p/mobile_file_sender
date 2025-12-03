package com.broadcastdata.main.background

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.broadcastdata.main.MainApplication
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundService @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted params: WorkerParameters
) : Worker(context, params) {
    private val TAG = "BackgroundService"
    private val BROADCAST_PORT = 8888

    //TODO move secret_key to build settings..?
    private val SECRET_KEY = "b3405fbdf36b61f06f3a91054c62d68cd963e45b658f0834e44d0b76e7659c4d"
    private val DISCOVER_TIMEOUT = 5000L // 5 seconds

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun doWork(): Result {

        var will_process = false

        context.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE)
            .getString(PREFS_WORKER_STATE, "false")?.let{
                will_process = it == "true"
            }

        val app = applicationContext as MainApplication
        val onRecieve = app.onBroadcastService

        if (!will_process){
            return Result.success()
        }

        try {
            discoverServer { host, port ->
                Log.i("WORKER_DONE", "found $host:$port")

                val prefs = applicationContext.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE)
                val fileUriString = prefs.getString(PREFS_URI_KEY, null)
                val dirName = prefs.getString(PREFS_CARNUM_KEY, "unknown")!!

                val fileUri = fileUriString?.toUri()
                runBlocking {
                    onRecieve.onSuccess(host, port, fileUri, dirName)
                }
            }

            scheduleNextWork(15, TimeUnit.MINUTES)
            return Result.success()

        }
        catch (e: Exception){
            return Result.failure()
        }
    }

    private fun scheduleNextWork(delay: Long, unit: TimeUnit){

        var is_working = false

        context.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE)
            .getString(PREFS_WORKER_STATE, "false")?.let{
                is_working = it == "true"
            }

        if (is_working){
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val retryRequest = OneTimeWorkRequestBuilder<BackgroundService>()
                .setInitialDelay(delay, unit)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork("upload-chain", ExistingWorkPolicy.REPLACE, retryRequest)
        }

    }

    private fun discoverServer(onServerFound: (String, Int) -> Unit) {
        var socket: DatagramSocket? = null
        try {
            socket = DatagramSocket().apply {
                soTimeout = DISCOVER_TIMEOUT.toInt()
                broadcast = true
            }

            Log.d(TAG, "Создан UDP сокет, timeout: $DISCOVER_TIMEOUT мс")

            // Отправляем broadcast сообщение
            val discoverMessage = "DISCOVER_$SECRET_KEY"
            val broadcastAddress = InetAddress.getByName("255.255.255.255")
            val sendData = discoverMessage.toByteArray()
            val sendPacket = DatagramPacket(
                sendData,
                sendData.size,
                broadcastAddress,
                BROADCAST_PORT
            )


            Log.d(TAG, "Отправка broadcast на 255.255.255.255:$BROADCAST_PORT")
            Log.d(TAG, "Сообщение: $discoverMessage")

            socket.send(sendPacket)
            Log.d(TAG, "Broadcast сообщение отправлено")

            // Ждем ответ
            val receiveData = ByteArray(1024)
            val receivePacket = DatagramPacket(receiveData, receiveData.size)


            Log.d(TAG, "Ожидаем ответ на порту ${socket.localPort}...")
            socket.receive(receivePacket)
            Log.d(TAG, "Ответ получен!")

            val response = String(receivePacket.data, 0, receivePacket.length).trim()
            Log.d(TAG, "Получен ответ: $response от ${receivePacket.address.hostAddress}")


            if (response.startsWith("FOUND_")) {
                val port = response.removePrefix("FOUND_").toIntOrNull() ?: 8000
                onServerFound(receivePacket.address.hostAddress, port)
            }

        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Таймаут при поиске сервера")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при поиске сервера: ${e.message}")
        } finally {
            socket?.close()
        }
    }

    companion object{
        const val WORKER_PREFS = "worker_presf"
        const val PREFS_URI_KEY = "file_uri"
        const val PREFS_WORKER_STATE = "worker_state"

        const val PREFS_NAME_KEY = "file_name"
        const val PREFS_CARNUM_KEY = "car_num"
    }
}