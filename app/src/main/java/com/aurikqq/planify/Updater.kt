package com.aurikqq.planify

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

suspend fun getLatestVersion(): String? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.github.com/repos/aurikqq/Planify/releases/latest")
        .header("Authorization", "token ${BuildConfig.GITHUB_TOKEN}")
        .build()

    val response = client.newCall(request).execute()
    val body = response.body.string()

    val json = JSONObject(body)
    json.getString("tag_name")
}

// org.json.JSONException: No value for assets

suspend fun downloadApk(context: Context, onProgress: (Float) -> Unit): File =
    withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/aurikqq/Planify/releases/latest"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()

        val json = JSONObject(body)
        val assets = json.getJSONArray("assets")
        if (assets.length() == 0) throw Exception("No assets found in release")

        var apkUrl: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                apkUrl = asset.getString("browser_download_url")
                break
            }
        }
        if (apkUrl == null) throw Exception("No APK asset found in release")

        val dlRequest = Request.Builder().url(apkUrl).build()
        val dlResponse = client.newCall(dlRequest).execute()
        val dlBody = dlResponse.body

        val apk = File(context.getExternalFilesDir(null), "update.apk")

        var bytesLoaded = 0L
        dlBody.byteStream().use { input ->
            FileOutputStream(apk).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytes = input.read(buffer)

                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    bytesLoaded += bytes
                    val progress = bytesLoaded.toFloat() / dlBody.contentLength()

                    withContext(Dispatchers.Main) {
                        onProgress(progress * 100f)
                    }
                    bytes = input.read(buffer)
                }
            }
        }
        apk
}

fun installApk(context: Context, apk: File?) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apk!!)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}

fun isNewVersionAvailable(currentVersion: String, latestVersion: String): Boolean {
    val current = currentVersion.removePrefix("v").split(".").map { it.toInt() }
    val latest = latestVersion.removePrefix("v").split(".").map { it.toInt() }
    var isNewerVersionAvailable = false

    for (i in 0 until minOf(current.size, latest.size)) {
        if (latest[i] > current[i]) {
            isNewerVersionAvailable = true
        }
    }
    return isNewerVersionAvailable
}

fun getCurrentVersion(context: Context): String? {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return packageInfo.versionName
}