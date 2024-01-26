package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.repository.FileRepository
import java.io.File

class DownloadFileUseCase(
    private val fileRepository: FileRepository
) {
    suspend fun execute(
        url: String,
        fileName: String,
        sessionId: String,
    ): File {
        return fileRepository.downloadFile(
            url = url,
            fileName = fileName,
            sessionId = sessionId,
        )
    }
}