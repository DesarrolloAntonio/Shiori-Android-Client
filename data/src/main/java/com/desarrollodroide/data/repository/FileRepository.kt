package com.desarrollodroide.data.repository

import java.io.File

interface FileRepository {
    suspend fun downloadFile(
        url: String,
        fileName: String,
        sessionId: String,
    ): File
}
