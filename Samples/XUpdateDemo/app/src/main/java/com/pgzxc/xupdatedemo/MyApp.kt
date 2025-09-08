package com.pgzxc.xupdatedemo

import OkHttpUpdateHttpService
import android.app.Application
import android.util.Log
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.utils.UpdateUtils

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 XUpdate
        XUpdate.get()
            .debug(true)
            .isWifiOnly(true) //默认设置只在wifi下检查版本更新
            .isGet(true) //默认设置使用get请求检查版本
            .isAutoMode(false) //默认设置非自动模式，可根据具体使用配置
            .param("versionCode", UpdateUtils.getVersionCode(this)) //设置默认公共请求参数
            .param("appKey", packageName)
            .setOnUpdateFailureListener { error ->
                //设置版本更新出错的监听
                if (error.getCode() !== CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                    Log.e("Error",error.toString())
                }
            }
            .supportSilentInstall(false) //设置是否支持静默安装，默认是true
            .setIUpdateHttpService(OkHttpUpdateHttpService()) //这个必须设置！实现网络请求功能。
            .init(this) //这个必须初始化
    }
}
