package com.aurikqq.planify

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LogsSenderActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                LogsSender().sendLogs(this@LogsSenderActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        }
    }
}

class LogsSender {
    fun collectLogcat() : String {
        val pid = Process.myPid()
        val process = Runtime.getRuntime().exec("logcat --pid=$pid -d")
        val reader = process.inputStream.bufferedReader()

        return reader.use { it.readText() }
    }

    fun Context.logcatToFile() : File {
        val file = File(cacheDir, "logcat.txt")
        file.writeText(collectLogcat())

        return file
    }

    suspend fun sendLogs(context: Context) {
        val file = context.logcatToFile()
        val token = "7851437249:AAHPR23Dt2CZaIZy_Yx7aVtKCye_onM2yWA"
        val chatId = "1104899353"

        val url = "https://api.telegram.org/bot$token/sendDocument"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart(
                "document",
                "logcat.txt",
                file.asRequestBody("text/plain".toMediaType())
            ).build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).execute()
    }
}