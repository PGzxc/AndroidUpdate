package com.pgzxc.updateforce

import android.app.*
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

class UpdateService : Service() {

    private var downloadId: Long = -1L
    private val handler = Handler()
    private val updateInterval = 500L // 每 500ms 更新一次通知

    private val channelId = "update_service_channel"
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val force = intent.getBooleanExtra(EXTRA_FORCE, false)

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // --- 创建通知渠道 ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "应用更新服务",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        // --- 初始化通知 ---
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("应用更新")
            .setContentText("准备下载...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, 0, true)

        startForeground(1, notificationBuilder.build())

        // --- 下载 APK ---
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("应用更新")
            setDescription("正在下载最新版本...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // 改这里
            setDestinationInExternalFilesDir(
                this@UpdateService,
                Environment.DIRECTORY_DOWNLOADS,
                "update.apk"
            )
            setMimeType("application/vnd.android.package-archive")
        }

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)
        FORCE_UPDATE = force

        // --- 启动下载进度更新 ---
        handler.post(progressRunnable)

        return START_STICKY
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun updateProgress() {
        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor? = dm.query(query)
        cursor?.use {
            if (it.moveToFirst()) {
                val total =
                    it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val downloaded =
                    it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                if (total > 0) {
                    val progress = (downloaded * 100 / total).toInt()
                    notificationBuilder.setProgress(100, progress, false).setContentText("下载中: $progress%")
                    notificationManager.notify(1, notificationBuilder.build())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_FORCE = "extra_force"
        var FORCE_UPDATE: Boolean = false

        fun newIntent(context: Context, url: String, force: Boolean): Intent {
            return Intent(context, UpdateService::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_FORCE, force)
            }
        }
    }
}
