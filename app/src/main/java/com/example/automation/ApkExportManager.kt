package com.example.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object ApkExportManager {

    private const val TAG = "ApkExportManager"
    private const val CHANNEL_ID = "mira_apk_download_channel"
    private const val NOTIFICATION_ID = 5050
    const val APK_FILE_NAME = "Mira-AI-Assistant.apk"

    fun getApkFileSize(context: Context): Float {
        return try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (sourceApk.exists()) {
                sourceApk.length() / (1024f * 1024f)
            } else {
                18.5f // Nominal APK size in MB
            }
        } catch (e: Exception) {
            18.5f
        }
    }

    suspend fun exportAndDownloadApk(
        context: Context,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                return@withContext Result.failure(Exception("Application APK source could not be accessed."))
            }

            val totalBytes = sourceApk.length().coerceAtLeast(1L)
            var bytesCopied = 0L

            // 1. Prepare local cache copy for fast FileProvider access and immediate install
            val apkCacheDir = File(context.cacheDir, "apks").apply { mkdirs() }
            val cachedApkFile = File(apkCacheDir, APK_FILE_NAME)

            // Copy to cache dir
            FileInputStream(sourceApk).use { input ->
                FileOutputStream(cachedApkFile).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        val progress = ((bytesCopied.toFloat() / totalBytes.toFloat()) * 50f).toInt()
                        onProgress(progress)
                    }
                    output.flush()
                }
            }

            // 2. Also write to public Downloads directory so user has the .apk file on device storage
            saveToPublicDownloads(context, cachedApkFile) { extraProgress ->
                onProgress(50 + (extraProgress / 2))
            }

            onProgress(100)

            // 3. Post system download completed notification with install action
            postDownloadCompleteNotification(context, cachedApkFile)

            Result.success(cachedApkFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export APK: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun saveToPublicDownloads(
        context: Context,
        cachedApk: File,
        onProgress: (Int) -> Unit
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, APK_FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(cachedApk).use { input ->
                            input.copyTo(out)
                        }
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                    val destFile = File(downloadsDir, APK_FILE_NAME)
                    FileInputStream(cachedApk).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            onProgress(100)
        } catch (e: Exception) {
            Log.w(TAG, "Public downloads copy warning (cache copy still valid): ${e.message}")
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK: ${e.message}")
            false
        }
    }

    fun shareApk(context: Context, apkFile: File): Boolean {
        return try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "Mira AI Assistant APK")
                putExtra(Intent.EXTRA_TEXT, "Download and install Mira AI Assistant on your Android phone!")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Share Mira APK via...").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing APK: ${e.message}")
            false
        }
    }

    private fun postDownloadCompleteNotification(context: Context, apkFile: File) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "APK Downloads",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for downloaded Mira APK package"
                }
                manager.createNotificationChannel(channel)
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                installIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Mira APK Downloaded")
                .setContentText("Mira-AI-Assistant.apk is ready in Downloads. Tap to install.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_upload, "Install Now", pendingIntent)
                .build()

            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Notification error: ${e.message}")
        }
    }
}
