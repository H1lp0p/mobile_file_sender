package com.broadcastdata.main.services

import android.net.Uri
import com.broadcastdata.main.actions.DirectoryWalker
import com.broadcastdata.main.actions.RequestSender
import com.broadcastdata.main.actions.SimplePushNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class OnBroadcastRecieveRequestImpl @Inject constructor(
    val pushNotification: SimplePushNotification,
    val directoryWalker: DirectoryWalker,
    val requestSender: RequestSender
) : OnBroadcastRecieveService {
    override suspend fun onSuccess(host: String, port: Int, dirUri: Uri?, dirName: String) {
        pushNotification.sendNotification(
            "Found server at $host:$port"
        )
        return withContext(Dispatchers.IO) {
            try {
                val response = requestSender.check(host, port)

                if (response.isSuccessful){

                    dirUri?.let {
                        val fileUris = directoryWalker.getDirectoryFiles(dirUri)

                        val fileCount = fileUris.size
                        val totalPercentage = fileCount * 100

                        pushNotification.showProgressNotification(progress = 0)

                        fileUris.forEachIndexed { ind, uri ->
                            val fileStream = directoryWalker.getFile(uri)

                            if (fileStream !== null) {

                                val fileName = directoryWalker.getFileName(uri)
                                val fileSize = directoryWalker.getFileSize(uri)

                                val response = requestSender.sendFile(
                                    host,
                                    port,
                                    fileStream,
                                    fileName!!,
                                    fileSize,
                                    dirName
                                ){ percentage ->
                                    val currentPercentage = 100 * ind + percentage
                                    pushNotification.updateProgress(((currentPercentage.toFloat() / totalPercentage) * 100).toInt())

                                    //Log.i("REQUEST_IMPL", "$uri -> $percentage")
                                }

                                if (!response.isSuccessful){
                                    pushNotification.sendNotification("NETWORK_ERR ${response.message}")
                                }

                            }
                            else{
                                pushNotification.sendNotification("Can't reach file $uri")
                            }

                            //Log.i("REQUEST_IMPL", "uri - $uri")
                        }
                        pushNotification.cancelProgress()
                    }
                }
                else{
                    pushNotification.sendNotification(
                        "Error on check request ${response.message}"
                    )
                }

            } catch (e: Exception) {
                pushNotification.sendNotification("Error: ${e.message}")
            }
        }
    }

    override suspend fun onFail() {
        pushNotification.sendNotification(
            "Something went wrong while trying to find host in local network"
        )
    }
}