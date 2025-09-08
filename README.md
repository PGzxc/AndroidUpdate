# AndroidUpdate
## 一 说明

```
两种实现Android的示例：
 1.自己实现应用升级
 2.借助三方库
```

## 二 Service服务器端(Python实现)

```
1、安装运行
pip install flask
python server.py

2、查看结果
http://192.168.8.221:5000/update.json

3、返回结果
{
  "ApkSize": 10240,
  "Code": 0,
  "DownloadUrl": "http://192.168.8.221:5000/app-release.apk",
  "ForceUpdate": true,
  "ModifyContent": "1. 修复若干 bug\n2. 优化性能",
  "Msg": "",
  "UpdateStatus": 1,
  "VersionCode": 2,
  "VersionName": "1.1"
}
```

## 三 Samples(两个项目，使用AS导入)

```
1、updateforce：
 -自己实现应用检测和升级
 -适配网络请求和FileProvider安装
 
2、XUpdateDemo
 - 借助三方库：XUpdate实现
 - 服务器返回接口内容符合XUpdate
 - 代码较少
```

## 四 示例效果

### 4.1 updateforce

![][1]

### 4.2 XUpdateDemo

![][2]

[1]:Gif/android-app-update-normal-3.gif
[2]:Gif/android-app-update-xupdate-5.gif