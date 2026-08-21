package com.desarrollodroide.data.repository

import android.util.Log
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.model.Tag
import com.desarrollodroide.network.model.SingleTagDTO
import com.desarrollodroide.network.model.TagPayloadDTO
import com.desarrollodroide.network.model.TagsDTO
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.NetworkNoCacheResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class TagsRepositoryImpl(
    private val apiService: RetrofitNetwork,
    private val tagsDao: TagDao,
    private val errorHandler: ErrorHandler
) : TagsRepository {

    override fun getTags(
        token: String,
        serverUrl: String
    ) = object :
        NetworkBoundResource<TagsDTO, List<Tag>>(errorHandler = errorHandler) {

        override suspend fun saveRemoteData(response: TagsDTO) {
            response.message?.map { it.toEntityModel() }?.let { tagsList ->
                tagsDao.replaceAllTags(tagsList)
            }
        }

        override fun fetchFromLocal(): Flow<List<Tag>> = tagsDao.getAllTags().map {
            it.map { it.toDomainModel() }
        }

        // with_bookmark_count is what fills TagDTO.bookmark_count; without it every tag comes
        // back with a zero count and the management screen has nothing to show. Servers that
        // predate the parameter ignore it.
        override suspend fun fetchFromRemote() = apiService.getTags(
            authorization = "Bearer $token",
            url = "${serverUrl.removeTrailingSlash()}/api/v1/tags?with_bookmark_count=true"
        )

        override fun shouldFetch(data: List<Tag>?) = true

    }.asFlow().flowOn(Dispatchers.IO)

    override fun createTag(
        token: String,
        serverUrl: String,
        name: String
    ) = object : NetworkNoCacheResource<SingleTagDTO, Tag>(errorHandler = errorHandler) {

        override suspend fun fetchFromRemote() = apiService.createTag(
            url = "${serverUrl.removeTrailingSlash()}/api/v1/tags",
            authorization = "Bearer $token",
            tag = TagPayloadDTO(name = name.trim())
        )

        override fun fetchResult(data: SingleTagDTO): Flow<Tag> = flow {
            val tag = data.message?.toDomainModel()
                ?: throw IllegalStateException("Create tag response did not contain a tag")
            tagsDao.insertTag(tag.toEntityModel())
            emit(tag)
        }

    }.asFlow().flowOn(Dispatchers.IO)

    override fun renameTag(
        token: String,
        serverUrl: String,
        tagId: Int,
        name: String
    ) = object : NetworkNoCacheResource<SingleTagDTO, Tag>(errorHandler = errorHandler) {

        override suspend fun fetchFromRemote() = apiService.updateTag(
            url = "${serverUrl.removeTrailingSlash()}/api/v1/tags/$tagId",
            authorization = "Bearer $token",
            tag = TagPayloadDTO(name = name.trim())
        )

        override fun fetchResult(data: SingleTagDTO): Flow<Tag> = flow {
            // The rename is applied locally rather than reinserting what came back: the response
            // carries no bookmark count, and overwriting the row would blank it in the list.
            tagsDao.renameTag(tagId = tagId, name = name.trim())
            emit(data.message?.toDomainModel() ?: Tag(id = tagId, name = name.trim()))
        }

    }.asFlow().flowOn(Dispatchers.IO)

    /**
     * Deleting a tag answers 204 with no body at all, because the server only wraps responses
     * that carry a JSON content type. Retrofit hands back a null body for 204, and
     * NetworkNoCacheResource treats a null body as failure, so this one is written out by hand:
     * success is the status code, there is nothing to deserialize.
     */
    override fun deleteTag(
        token: String,
        serverUrl: String,
        tagId: Int
    ): Flow<Result<Unit>> = flow {
        emit(Result.Loading(null))
        try {
            val response = apiService.deleteTag(
                url = "${serverUrl.removeTrailingSlash()}/api/v1/tags/$tagId",
                authorization = "Bearer $token"
            )
            if (response.isSuccessful) {
                tagsDao.deleteTagById(tagId)
                emit(Result.Success(Unit))
            } else {
                emit(
                    Result.Error(
                        errorHandler.getApiError(
                            statusCode = response.code(),
                            throwable = null,
                            message = response.errorBody()?.string()
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(Result.Error(errorHandler.getError(e), null))
        }
    }.flowOn(Dispatchers.IO)

    override fun getLocalTags(): Flow<List<Tag>> {
        return tagsDao.observeAllTags()
            .onEach { entities ->
                Log.d("TagsRepository", "Tags updated in repository: ${entities.size}")
            }
            .map { entities ->
                entities.map { it.toDomainModel() }
            }
    }

}

