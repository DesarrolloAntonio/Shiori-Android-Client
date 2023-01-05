package com.shiori.domain.repository

import com.shiori.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow
import com.shiori.domain.model.state.Result

interface MainRepository {

  fun getDummyObjects(): Flow<Result<out List<Bookmark>?>>

}