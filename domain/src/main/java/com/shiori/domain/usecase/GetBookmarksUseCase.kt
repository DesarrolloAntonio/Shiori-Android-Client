package com.shiori.domain.usecase

import com.shiori.data.repository.MainRepository
import com.shiori.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetBookmarksUseCase(
    private val mainRepository: MainRepository,
) : SuspendUseCase<Unit, Flow<List<Bookmark>>> {

    override fun execute(params: Unit): Flow<List<Bookmark>>{
        return mainRepository.getBookmarks()
//        return mainRepository.getBookmarks().mapNotNull { response ->
//            val result = response?.map {
////                Bookmark(
////                    id = it.id,
////                     url = it.url,
////                 title = it.title,
////                 excerpt = it.excerpt,
////                 author = it.author,
////                 public = it.public,
////                 modified = it.modified,
////                 imageURL = it.imageURL,
////                 hasContent = it.hasContent,
////                 hasArchive = it.hasArchive,
////                 tags = it.tags.map { Tag(
////                     id = it.id,
////                     name = it.name
////                 ) },
////                 createArchive = it.createArchive
////                )
//            }
//            return@mapNotNull when (response) {
//                is Result.Success -> Result.Success(result)
//                is Result.Error -> Result.Error(response.error, result)
//                is Result.Loading -> Result.Loading(result)
//            }
//        }
    }
}