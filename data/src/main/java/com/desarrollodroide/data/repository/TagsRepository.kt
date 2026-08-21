package com.desarrollodroide.data.repository

import kotlinx.coroutines.flow.Flow
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.Tag

interface TagsRepository {

  fun getTags(
    token: String,
    serverUrl: String
  ): Flow<Result<List<Tag>?>>

  fun getLocalTags(): Flow<List<Tag>>

  fun createTag(
    token: String,
    serverUrl: String,
    name: String
  ): Flow<Result<Tag?>>

  fun renameTag(
    token: String,
    serverUrl: String,
    tagId: Int,
    name: String
  ): Flow<Result<Tag?>>

  fun deleteTag(
    token: String,
    serverUrl: String,
    tagId: Int
  ): Flow<Result<Unit>>
}