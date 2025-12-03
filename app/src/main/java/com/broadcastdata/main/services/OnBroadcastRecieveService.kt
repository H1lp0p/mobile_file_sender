package com.broadcastdata.main.services

import android.net.Uri

interface OnBroadcastRecieveService {
    suspend fun onSuccess(host: String, port: Int, dirUri: Uri?, dirName: String): Unit
    suspend fun onFail(): Unit
}