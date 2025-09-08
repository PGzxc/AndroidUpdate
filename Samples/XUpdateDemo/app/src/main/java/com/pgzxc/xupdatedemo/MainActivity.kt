package com.pgzxc.xupdatedemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xuexiang.xupdate.XUpdate

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启动时检查更新
        XUpdate.newBuild(this)
            .updateUrl("http://192.168.8.221:5000/update.json") // 你的更新接口
            //.isAutoMode(true) //如果需要完全无人干预，自动更新，需要root权限【静默安装需要】
            //.supportBackgroundUpdate(true) //支持后台更新
            .update()
    }
}