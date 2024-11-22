package com.desarrollodroide.data.repository

import android.util.Log
import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.data.mapper.*
import com.desarrollodroide.model.Tag
import com.desarrollodroide.network.model.TagsDTO
import com.desarrollodroide.network.retrofit.NetworkBoundResource
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
                tagsDao.deleteAllTags()
                tagsDao.insertAllTags(tagsList)
            }
        }

        override fun fetchFromLocal(): Flow<List<Tag>> = tagsDao.getAllTags().map {
            it.map { it.toDomainModel() }
        }

        override suspend fun fetchFromRemote() = apiService.getTags(
            authorization = "Bearer $token",
            url = "${serverUrl.removeTrailingSlash()}/api/v1/tags"
        )

        override fun shouldFetch(data: List<Tag>?) = true

    }.asFlow().flowOn(Dispatchers.IO)

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

