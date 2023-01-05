package com.shiori.domain.usecase

import com.shiori.domain.model.Bookmark
import com.shiori.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import com.shiori.domain.model.state.Result

class GetBookmarksUseCase @Inject constructor(
    private val dummyObjectRepository: MainRepository,
) : UseCase<Unit, Flow<Result<List<Bookmark>?>>> {

    override fun execute(params: Unit): Flow<Result<List<Bookmark>?>> {
        return dummyObjectRepository.getDummyObjects().mapNotNull { response ->
            val result = response.data
            return@mapNotNull when (response) {
                is Result.Success -> Result.Success(result)
                is Result.Error -> Result.Error(response.error, result)
                is Result.Loading -> Result.Loading(result)
            }
        }
    }
}