package com.desarrollodroide.domain.usecase

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.repository.TagsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class DeleteTagUseCase(
    private val tagsRepository: TagsRepository
) {
    operator fun invoke(
        serverUrl: String,
        token: String,
        tagId: Int,
    ): Flow<Result<Unit>> = tagsRepository.deleteTag(
        token = token,
        serverUrl = serverUrl,
        tagId = tagId,
    ).flowOn(Dispatchers.IO)
}
