package com.pgzxc.updateforce

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Process
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.File

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = dm.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriStr = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val apkFile = File(Uri.parse(uriStr).path!!)

                    val force = UpdateService.FORCE_UPDATE

                    // 弹窗提示安装
                    AlertDialog.Builder(context)
                        .setTitle("更新提示")
                        .setMessage(if (force) "必须更新才能继续使用" else "新版本已下载，是否立即安装？")
                        .setPositiveButton("安装") { _, _ -> installApk(context, apkFile) }
                        .setNegativeButton(if (force) "退出" else "稍后") { _, _ ->
                            if (force) {
                                // 退出整个应用
                                Process.killProcess(Process.myPid())
                            }
                        }
                        .setCancelable(!force)
                        .create()
                        .show()
                }
            }
            cursor.close()
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
