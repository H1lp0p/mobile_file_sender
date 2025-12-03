package com.broadcastdata.main.actions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.broadcastdata.main.MainActivity

class SimplePushNotification(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "file_sender_channel"
        const val CHANNEL_NAME = "File Sender Notifications"

        const val PROGRESS_CHANNEL_ID = "file_sender_progress_channel"

        const val PROGRESS_CHANNEL_NAME = "File Transfer Progress"
        const val NOTIFICATION_ID = 1

        const val PROGRESS_NOTIFICATION_ID = 2

        const val TAG = "SimplePushNotif"
    }

    init {
        createNotificationChannel()
        createProgressNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for file transfer operations"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createProgressNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PROGRESS_CHANNEL_ID,
                PROGRESS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // LOW чтобы не беспокоить во время загрузки
            ).apply {
                description = "Notifications for file transfer progress"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

    }

    /**
     * Основной метод для отправки push-уведомления
     * @param message Текст сообщения для уведомления
     * @param title Заголовок уведомления (опционально)
     * @param intent Intent для открытия при клике (опционально)
     */
    fun sendNotification(
        message: String,
        title: String = "File Sender",
        intent: Intent? = null
    ) {
        // Создаем PendingIntent для обработки клика по уведомлению
        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            // Если intent не передан, открываем MainActivity
            val defaultIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            PendingIntent.getActivity(
                context,
                0,
                defaultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Строим уведомление
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Замените на свою иконку
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Уведомление исчезает при клике
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Показываем уведомление
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Метод для отображения уведомления с прогрессом загрузки
     * @param progress Текущий прогресс (0-100)
     * @param currentBytes Текущее количество загруженных байт (опционально)
     * @param totalBytes Общее количество байт для загрузки (опционально)
     * @param filename Имя файла (опционально)
     * @param title Заголовок уведомления (опционально)
     * @param indeterminate Флаг неопределенного прогресса (когда неизвестно общее время/размер)
     */
    fun showProgressNotification(
        progress: Int,
        currentBytes: Long? = null,
        totalBytes: Long? = null,
        filename: String? = null,
        title: String = "Загрузка файла",
        indeterminate: Boolean = false
    ) {
        // Формируем текст уведомления
        val contentText = buildString {
            filename?.let { append("Файл: $it") }

            if (currentBytes != null && totalBytes != null) {
                if (isNotEmpty()) append("\n")
                append("${formatBytes(currentBytes)} / ${formatBytes(totalBytes)}")
            }

            if (isEmpty()) {
                append("Прогресс: $progress%")
            }
        }

        // Создаем интент для открытия приложения
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Строим уведомление с прогрессом
        val notification = NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Замените на свою иконку
            .setContentIntent(pendingIntent)
            .setProgress(100, progress, indeterminate)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Уведомление нельзя смахнуть
            .setAutoCancel(false) // Не автоматически закрывается
            .setOnlyAlertOnce(true) // Звук/вибрация только при первом показе
            .build()

        // Показываем уведомление
        notificationManager.notify(PROGRESS_NOTIFICATION_ID, notification)
    }

    /**
     * Метод для обновления прогресса загрузки
     * @param progress Текущий прогресс (0-100)
     * @param currentBytes Текущее количество загруженных байт (опционально)
     * @param totalBytes Общее количество байт для загрузки (опционально)
     */
    fun updateProgress(
        progress: Int,
        currentBytes: Long? = null,
        totalBytes: Long? = null
    ) {
        showProgressNotification(progress, currentBytes, totalBytes)
    }

    /**
     * Метод для завершения прогресса и показа результата
     * @param success Успешно ли завершена загрузка
     * @param message Сообщение о результате
     * @param filename Имя файла (опционально)
     */
    fun completeProgress(
        success: Boolean,
        message: String = if (success) "Загрузка завершена" else "Ошибка загрузки",
        filename: String? = null
    ) {
        // Сначала удаляем уведомление с прогрессом
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)

        // Показываем финальное уведомление
        val title = if (success) "Загрузка завершена" else "Ошибка загрузки"
        val finalMessage = buildString {
            append(message)
            filename?.let { append("\nФайл: $it") }
        }

        sendNotification(finalMessage, title)
    }

    /**
     * Метод для отмены прогресса (скрывает уведомление)
     */
    fun cancelProgress() {
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    /**
     * Вспомогательный метод для форматирования байтов в читаемый вид
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${String.format("%.1f", bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))} MB"
            else -> "${String.format("%.1f", bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
}