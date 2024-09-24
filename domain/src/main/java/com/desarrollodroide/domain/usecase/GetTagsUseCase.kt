package com.desarrollodroide.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.TagsRepository
import com.desarrollodroide.model.Tag

class GetTagsUseCase(
    private val tagsRepository: TagsRepository
) {
    operator fun invoke(
        serverUrl: String,
        token: String,
    ): Flow<Result<List<Tag>?>> {
        return tagsRepository.getTags(
            token = token,
            serverUrl = serverUrl
        ).flowOn(Dispatchers.IO)
    }

    fun getLocalTags(): Flow<List<Tag>> {
        return tagsRepository.getLocalTags()
            .flowOn(Dispatchers.IO)
    }
}