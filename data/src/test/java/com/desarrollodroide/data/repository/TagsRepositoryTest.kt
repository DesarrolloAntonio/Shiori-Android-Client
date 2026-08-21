package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.room.dao.TagDao
import com.desarrollodroide.network.model.SingleTagDTO
import com.desarrollodroide.network.model.TagDTO
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import retrofit2.Response

@ExperimentalCoroutinesApi
class TagsRepositoryImplTest {

    @Mock
    private lateinit var apiService: RetrofitNetwork

    @Mock
    private lateinit var tagsDao: TagDao

    @Mock
    private lateinit var errorHandler: ErrorHandler

    private lateinit var tagsRepository: TagsRepositoryImpl

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        tagsRepository = TagsRepositoryImpl(apiService, tagsDao, errorHandler)
    }

    @Test
    fun `createTag should cache the created tag and emit it`() = runTest {
        // Arrange
        val created = SingleTagDTO(ok = true, message = TagDTO(id = 7, name = "kotlin", nBookmarks = 0))
        `when`(apiService.createTag(anyString(), anyString(), any())).thenReturn(Response.success(created))

        // Act
        val results = tagsRepository.createTag("token", "http://test.com", "kotlin").toList()

        // Assert
        assertEquals(2, results.size, "Expected 2 emitted results")
        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals("kotlin", results[1].data?.name)

        verify(tagsDao).insertTag(check { assertEquals(7, it.id) })
        verify(apiService).createTag(
            check { assertTrue(it.endsWith("/api/v1/tags")) },
            eq("Bearer token"),
            check { assertEquals("kotlin", it.name) }
        )
    }

    @Test
    fun `createTag should trim the name before sending it`() = runTest {
        // Arrange
        val created = SingleTagDTO(ok = true, message = TagDTO(id = 1, name = "kotlin", nBookmarks = 0))
        `when`(apiService.createTag(anyString(), anyString(), any())).thenReturn(Response.success(created))

        // Act
        tagsRepository.createTag("token", "http://test.com", "  kotlin  ").toList()

        // Assert
        verify(apiService).createTag(anyString(), anyString(), check { assertEquals("kotlin", it.name) })
    }

    @Test
    fun `renameTag should update the cached row rather than replacing it`() = runTest {
        // Arrange: the rename response carries no bookmark count, so reinserting the row would
        // blank the count the list shows.
        val renamed = SingleTagDTO(ok = true, message = TagDTO(id = 3, name = "android", nBookmarks = null))
        `when`(apiService.updateTag(anyString(), anyString(), any())).thenReturn(Response.success(renamed))

        // Act
        val results = tagsRepository.renameTag("token", "http://test.com", 3, "android").toList()

        // Assert
        assertTrue(results.last() is Result.Success)
        verify(tagsDao).renameTag(eq(3), eq("android"))
        verify(tagsDao, never()).insertTag(any())
        verify(apiService).updateTag(
            check { assertTrue(it.endsWith("/api/v1/tags/3")) },
            eq("Bearer token"),
            any()
        )
    }

    @Test
    fun `deleteTag should remove the cached row`() = runTest {
        // Arrange
        `when`(apiService.deleteTag(anyString(), anyString())).thenReturn(Response.success(Unit))

        // Act
        val results = tagsRepository.deleteTag("token", "http://test.com", 5).toList()

        // Assert
        assertTrue(results.last() is Result.Success)
        verify(tagsDao).deleteTagById(5)
        verify(apiService).deleteTag(check { assertTrue(it.endsWith("/api/v1/tags/5")) }, eq("Bearer token"))
    }

    @Test
    fun `deleteTag should leave the cache alone when the server rejects it`() = runTest {
        // Arrange
        val errorBody = "Tag not found".toResponseBody("text/plain".toMediaTypeOrNull())
        `when`(apiService.deleteTag(anyString(), anyString())).thenReturn(Response.error(404, errorBody))
        `when`(errorHandler.getApiError(eq(404), anyOrNull(), anyOrNull()))
            .thenReturn(Result.ErrorType.HttpError(statusCode = 404))

        // Act
        val results = tagsRepository.deleteTag("token", "http://test.com", 5).toList()

        // Assert
        assertTrue(results.last() is Result.Error)
        verify(tagsDao, never()).deleteTagById(any())
    }

    private fun anyString(): String = org.mockito.kotlin.any()
}
