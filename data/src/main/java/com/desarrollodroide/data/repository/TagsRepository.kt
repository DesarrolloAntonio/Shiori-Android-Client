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
}