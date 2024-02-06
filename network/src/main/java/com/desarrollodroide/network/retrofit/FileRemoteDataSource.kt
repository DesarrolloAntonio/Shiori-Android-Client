package com.desarrollodroide.network.retrofit

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import android.os.Environment

class FileRemoteDataSource {
    suspend fun downloadFile(
        url: String,
        fileName: String,
        sessionId: String
    ): File {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Session-Id", sessionId)
            .build()

        val response = client.newCall(request).execute()
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val downloadedFile = File(directory, "${cleanFileName(fileName)}.epub")

        response.body?.byteStream().use { input ->
            downloadedFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }

        return downloadedFile
    }

    private fun cleanFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9.,\\-\\s_]"), "_")
    }
}

