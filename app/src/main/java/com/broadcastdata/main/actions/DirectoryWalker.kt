package com.broadcastdata.main.actions

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.InputStream
import androidx.documentfile.provider.DocumentFile

class DirectoryWalker(private val context: Context) {
    fun getDirectoryFiles(directoryUri: Uri): List<Uri>{
        val files = mutableListOf<Uri>()

        val dirDocFile = DocumentFile.fromTreeUri(context, directoryUri)

        if (dirDocFile != null && dirDocFile.exists()) {
            for (fileDoc in dirDocFile.listFiles()) {
                if (!fileDoc.isDirectory) {
                    files.add(fileDoc.uri)
                }
            }
        }
        return files
    }

    fun getFile(uri: Uri) : InputStream?{
        return context.contentResolver.openInputStream(uri)
    }

    // Вспомогательные функции для работы с Uri
    fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcel ->
                parcel.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    cursor.getString(displayNameIndex)
                } else {
                    uri.lastPathSegment
                }
            } else {
                uri.lastPathSegment
            }
        }
    }

    companion object{
        private const val TAG = "DirWalker"
    }
}