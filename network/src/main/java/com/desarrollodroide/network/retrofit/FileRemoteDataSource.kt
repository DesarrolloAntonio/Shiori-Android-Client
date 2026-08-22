package com.desarrollodroide.network.retrofit

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Downloads a bookmark's epub.
 *
 * Takes the shared client rather than building its own. The one it used to build had none of the
 * app's interceptors, so the `X-Session-Id` header never became the `Authorization: Bearer` that
 * v1.8.0 wants, and the request went out unauthenticated. The server answers those by serving the
 * web app's index.html, and this then wrote that html to disk with an .epub extension and reported
 * success. The file was 8kb of `<!DOCTYPE html>`.
 */
class FileRemoteDataSource(
    private val client: OkHttpClient,
) {
    fun downloadFile(
        context: Context,
        url: String,
        fileName: String,
        sessionId: String
    ): File {
        val request = Request.Builder()
            .url(url)
            .addHeader("X-Session-Id", sessionId)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Could not download the ebook: HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("The ebook response had no body")
            val bytes = body.bytes()

            // An epub is a zip. Anything else here is the server answering with something other
            // than the file, and saving it would leave an unopenable download behind.
            if (!bytes.startsWithZipMagic()) {
                throw IOException(
                    "The server did not return an ebook for this bookmark. " +
                        "It may not have been generated yet."
                )
            }

            val directory = context.getExternalFilesDir(null)
            val downloadedFile = File(directory, "${cleanFileName(fileName)}.epub")
            downloadedFile.writeBytes(bytes)
            return downloadedFile
        }
    }

    private fun ByteArray.startsWithZipMagic(): Boolean =
        size >= 2 && this[0] == 'P'.code.toByte() && this[1] == 'K'.code.toByte()

    private fun cleanFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9.,\\-\\s_؀-ۿ]"), "_")
    }
}
