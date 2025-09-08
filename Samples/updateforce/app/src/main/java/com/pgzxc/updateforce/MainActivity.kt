package com.pgzxc.updateforce

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val receiver = UpdateReceiver()
    private val client = OkHttpClient()
    private val updateUrl = "http://192.168.8.221:5000/update.json"

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 注册下载完成广播
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        // 异步检查更新
        GlobalScope.launch(Dispatchers.IO) {
            checkUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private suspend fun checkUpdate() {
        try {
            val request = Request.Builder().url(updateUrl).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return
            val json = JSONObject(body)

            val latestVersionCode = json.getInt("VersionCode")
            val forceUpdate = json.getBoolean("ForceUpdate")
            val apkUrl = json.getString("DownloadUrl")

            if (latestVersionCode > BuildConfig.VERSION_CODE) {
                withContext(Dispatchers.Main) {
                    if (isWifiConnected()) {
                        // Wi-Fi 自动后台下载
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            startForegroundService(UpdateService.newIntent(this@MainActivity, apkUrl, forceUpdate))
                        } else {
                            startService(UpdateService.newIntent(this@MainActivity, apkUrl, forceUpdate))
                        }
                    } else {
                        // 非 Wi-Fi 弹窗提示
                        showUpdateDialog(forceUpdate, apkUrl)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isWifiConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun showUpdateDialog(force: Boolean, apkUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("发现新版本")
            .setMessage(if (force) "必须更新才能继续使用" else "是否立即更新？")
            .setPositiveButton("更新") { _, _ ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(UpdateService.newIntent(this, apkUrl, force))
                } else {
                    startService(UpdateService.newIntent(this, apkUrl, force))
                }
            }
            .setNegativeButton("退出") { _, _ ->
                if (force) finishAffinity()
            }
            .setCancelable(!force)
            .show()
    }
}
