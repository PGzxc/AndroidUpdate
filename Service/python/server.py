from flask import Flask, jsonify, send_from_directory
import os

app = Flask(__name__)

APK_FILE = "app-release.apk"  # APK 文件名
APK_DIR = os.path.abspath(".") # APK 所在目录，这里假设和 app.py 同目录

@app.route("/update.json")
def update():
    return jsonify({
        "Code": 0,
        "Msg": "",
        "UpdateStatus": 1,   # 1=有更新, 0=无更新
        "VersionCode": 2,
        "VersionName": "1.1",
        "ModifyContent": "1. 修复若干 bug\n2. 优化性能",
        "DownloadUrl": f"http://192.168.8.221:5000/{APK_FILE}",
        "ApkSize": 10240,
        "ForceUpdate": True  # true=强制更新, false=可选更新
    })

@app.route(f"/{APK_FILE}")
def download_apk():
    return send_from_directory(APK_DIR, APK_FILE, as_attachment=True)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
