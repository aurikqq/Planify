package com.aurikqq.planify

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.round

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

suspend fun downloadApk(context: Context, onProgress: (Float) -> Unit): File =
    withContext(Dispatchers.IO) {
        val url = ("https://api.github.com/repos/aurikqq/Planify/releases/latest")
        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "token ${BuildConfig.GITHUB_TOKEN}")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body.string()

        val json = JSONObject(body)
        val assets = json.getJSONArray("assets")
        if (assets.length() == 0) throw Exception("No assets found in release")

        var assetId: Int?
        var apkUrl: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                assetId = asset.getInt("id")
                apkUrl = "https://api.github.com/repos/aurikqq/Planify/releases/assets/$assetId"
                break
            }
        }
        if (apkUrl == null) throw Exception("No APK asset found in release")

        val dlRequest = Request.Builder()
            .url(apkUrl)
            .header("Accept", "application/octet-stream")
            .header("Authorization", "token ${BuildConfig.GITHUB_TOKEN}")
            .header("User-Agent", "PlanifyAutoUpdater")
            .build()
        val dlResponse = client.newCall(dlRequest).execute()
        val dlBody = dlResponse.body

        if (!dlResponse.isSuccessful) {
            throw IOException("Failed to download APK: ${dlResponse.code} ${dlResponse.message} ${dlBody.string()}")
        }

        val totalBytes = dlBody.contentLength()
        val isProgressAvailable = totalBytes > 0

        Log.d("Updater", "Downloading from $apkUrl")
        Log.d("Updater", "Response code: ${dlResponse.code}")

        val apk = File(context.getExternalFilesDir(null), "update.apk")

        var bytesLoaded = 0L
        dlBody.byteStream().use { input ->
            FileOutputStream(apk).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytes = input.read(buffer)

                while (bytes >= 0) {
                    output.write(buffer, 0, bytes)
                    bytesLoaded += bytes
                    if (isProgressAvailable) {
                        val progress = bytesLoaded.toFloat() / totalBytes
                        withContext(Dispatchers.Main) {
                            onProgress(round(progress * 100))
                        }
                    }
                    else {
                        withContext(Dispatchers.Main) {
                            onProgress(-1f)
                        }
                    }
                    bytes = input.read(buffer)
                }
            }
        }
        apk
}

fun installApk(context: Context, apk: File?): Boolean {
    if (!context.packageManager.canRequestPackageInstalls()) {
        val intent = Intent(
            android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri()
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return false
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apk!!)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
    return true
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