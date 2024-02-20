package com.desarrollodroide.data.repository

import android.content.Context
import com.desarrollodroide.network.retrofit.FileRemoteDataSource
import java.io.File

class FileRepositoryImpl(
    private val context: Context,
    private val remoteDataSource: FileRemoteDataSource
) : FileRepository {
    override suspend fun downloadFile(
        url: String,
        fileName: String,
        sessionId: String,
    ): File {
        return remoteDataSource.downloadFile(
            context = context,
            url = url,
            fileName = fileName,
            sessionId = sessionId
        )
    }
}


