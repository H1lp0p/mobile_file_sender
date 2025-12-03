package com.broadcastdata.main.background

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class WifiNetworkCallback(private val context: Context) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        // Сеть доступна
        checkNetworkType()
    }

    override fun onLost(network: Network) {
        // Сеть потеряна
        checkNetworkType()
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        // Изменились возможности сети (например, переключились между WiFi/mobile)
        checkNetworkType()
    }

    private fun checkNetworkType() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (isWifi) {
            startWifiWorker()
        }
    }

    private fun startWifiWorker() {
        val workRequest = OneTimeWorkRequestBuilder<BackgroundService>()
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}