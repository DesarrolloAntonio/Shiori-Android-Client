package com.desarrollodroide.domain.usecase

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.TagsRepository
import com.desarrollodroide.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class CreateTagUseCase(
    private val tagsRepository: TagsRepository
) {
    operator fun invoke(
        serverUrl: String,
        token: String,
        name: String,
    ): Flow<Result<Tag?>> = tagsRepository.createTag(
        token = token,
        serverUrl = serverUrl,
        name = name,
    ).flowOn(Dispatchers.IO)
}
