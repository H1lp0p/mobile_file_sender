package com.broadcastdata.main.actions

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.InputStream

class DirectoryWalker(private val context: Context) {
    fun getDirectoryFiles(directoryUri: Uri): List<Uri>{
        val files = mutableListOf<Uri>()
        val contentResolver = context.contentResolver

        // Получаем documentId из tree URI
        val documentId = DocumentsContract.getTreeDocumentId(directoryUri)

        // Строим URI для дочерних документов
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(directoryUri, documentId)

        contentResolver.query(
            childrenUri,  // Используем childrenUri вместо directoryUri
            null, null, null, null
        )?.use { cursor ->
            val displayNameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val documentIdIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

            while (cursor.moveToNext()) {
                val mimeType = cursor.getString(mimeTypeIndex)
                val displayName = cursor.getString(displayNameIndex)
                val childDocumentId = cursor.getString(documentIdIndex)

                // Игнорируем директории, берем только файлы
                if (mimeType != DocumentsContract.Document.MIME_TYPE_DIR) {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(
                        directoryUri,
                        childDocumentId  // Используем documentId из курсора
                    )
                    files.add(fileUri)
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