package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.model.LivenessResponse
import kotlinx.coroutines.flow.Flow

interface SystemRepository {

    fun liveness(
      serverUrl: String
    ): Flow<Result<LivenessResponse?>>
}