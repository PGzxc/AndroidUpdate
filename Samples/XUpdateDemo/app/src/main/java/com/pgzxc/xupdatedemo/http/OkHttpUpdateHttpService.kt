import com.xuexiang.xupdate.proxy.IUpdateHttpService
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class OkHttpUpdateHttpService : IUpdateHttpService {

    private val client = OkHttpClient()

    override fun asyncGet(
            url: String,
            params: MutableMap<String, Any>,
            callBack: IUpdateHttpService.Callback
    ) {
        val httpUrlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        for ((key, value) in params) {
            httpUrlBuilder.addQueryParameter(key, value.toString())
        }
        val request = Request.Builder().url(httpUrlBuilder.build()).get().build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callBack.onSuccess(response.body?.string())
            }
        })
    }

    override fun asyncPost(
            url: String,
            params: MutableMap<String, Any>,
            callBack: IUpdateHttpService.Callback
    ) {
        val formBodyBuilder = FormBody.Builder()
        for ((key, value) in params) {
            formBodyBuilder.add(key, value.toString())
        }
        val request = Request.Builder().url(url).post(formBodyBuilder.build()).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                callBack.onSuccess(response.body?.string())
            }
        })
    }

    override fun download(
            url: String,
            path: String,
            fileName: String,
            callBack: IUpdateHttpService.DownloadCallback
    ) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callBack.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val dir = File(path)
                if (!dir.exists()) {
                    dir.mkdirs()   // 创建目录
                }
                val file = File(dir, fileName)

                val inputStream = response.body?.byteStream()
                val outputStream = FileOutputStream(file)

                try {
                    val buffer = ByteArray(2048)
                    var total = 0L
                    val contentLength = response.body?.contentLength() ?: -1

                    var len: Int
                    while (inputStream!!.read(buffer).also { len = it } != -1) {
                        total += len
                        outputStream.write(buffer, 0, len)
                        callBack.onProgress(total.toFloat() / contentLength, contentLength)
                    }
                    outputStream.flush()
                    callBack.onSuccess(file)
                } catch (e: Exception) {
                    callBack.onError(e)
                } finally {
                    inputStream?.close()
                    outputStream.close()
                }
            }

        })
    }

    override fun cancelDownload(url: String) {
        // 简单处理：找到对应 call 并取消
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().url.toString() == url) {
                call.cancel()
            }
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().url.toString() == url) {
                call.cancel()
            }
        }
    }
}
