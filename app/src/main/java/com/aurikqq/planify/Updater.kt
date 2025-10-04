package com.aurikqq.planify

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

suspend fun getLatestVersion(): String? {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.github.com/repos/aurikqq/Planify/releases/latest")
        .build()

    val response = client.newCall(request).execute()
    val body = response.body.string()

    val json = JSONObject(body)
    return json.getString("tag_name")
}

suspend fun downloadApk(context: Context): File {
    val url = "https://api.github.com/repos/aurikqq/Planify/releases/latest"
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()
    val response = client.newCall(request).execute()

    val apk = File(context.getExternalFilesDir(null), "update.apk")
    response.body.byteStream().use { input ->
        FileOutputStream(apk).use { output ->
            input.copyTo(output)
        }
    }
    return apk
}

fun installApk(context: Context, apk: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apk)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}

fun isNewVersionAvailable(currentVersion: String, latestVersion: String): Boolean {
    val current = currentVersion.removePrefix("v").split(".").map { it.toInt() }
    val latest = latestVersion.removePrefix("v").split(".").map { it.toInt() }

    for (i in 0 until minOf(current.size, latest.size)) {
        if (latest[i] > current[i]) return true
        else return false
    }
    return latest.size > current.size
}

fun getCurrentVersion(context: Context): String? {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return packageInfo.versionName
}