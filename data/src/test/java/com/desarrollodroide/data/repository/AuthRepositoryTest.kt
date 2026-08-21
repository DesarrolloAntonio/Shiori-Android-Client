package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import com.desarrollodroide.network.model.AccountDTO
import com.desarrollodroide.network.model.SessionDTO
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito.*
import retrofit2.Response
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.check
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.network.model.LoginResponseDTO
import com.desarrollodroide.network.model.LoginResponseMessageDTO
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.mockito.kotlin.anyOrNull
import java.io.IOException

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {

    @Mock
    private lateinit var apiService: RetrofitNetwork

    @Mock
    private lateinit var settingsPreferenceDataSource: SettingsPreferenceDataSource

    @Mock
    private lateinit var errorHandler: ErrorHandler

    private lateinit var authRepository: AuthRepositoryImpl

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepositoryImpl(apiService, settingsPreferenceDataSource, errorHandler)
    }

    @Test
    fun `sendLogout should emit Loading, Loading with data, and Success states when API call is successful`() = runTest {
        // Arrange
        val serverUrl = "http://test.com"
        val xSession = "testSession"
        val logoutResponse = "Logout successful" // La respuesta esperada del servidor

        `when`(apiService.sendLogout(anyString(), anyString())).thenReturn(Response.success(logoutResponse))

        // Act
        val results = authRepository.sendLogout(serverUrl, xSession).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null, "First result should be Loading with null data")
        assertTrue(results[1] is Result.Loading && results[1].data == "", "Second result should be Loading with empty data")
        assertTrue(results[2] is Result.Success && (results[2] as Result.Success).data == "") {
            "Expected third result to be Success with empty data after resetUser, but was '${(results[2] as Result.Success).data}'"
        }

        verify(settingsPreferenceDataSource).resetData()
        verify(apiService).sendLogout(check { it.endsWith("/api/v1/auth/logout") }, eq(xSession))
    }

    @Test
    fun `sendLogout should emit Loading and Error states when API call fails`() = runTest {
        // Arrange
        val serverUrl = "http://test.com"
        val xSession = "testSession"
        val errorMessage = "Logout failed"
        val errorResponseBody = errorMessage.toResponseBody("text/plain".toMediaTypeOrNull())

        `when`(apiService.sendLogout(anyString(), anyString())).thenReturn(Response.error(400, errorResponseBody))
        `when`(errorHandler.getApiError(eq(400), anyOrNull(), eq(errorMessage))).thenReturn(Result.ErrorType.HttpError(statusCode = 400, message = errorMessage))

        // Act
        val results = authRepository.sendLogout(serverUrl, xSession).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null, "First result should be Loading with null data")
        assertTrue(results[1] is Result.Loading && results[1].data == "", "Second result should be Loading with empty string data")
        assertTrue(results[2] is Result.Error && (results[2] as Result.Error).error is Result.ErrorType.HttpError, "Third result should be Error with HttpError type")
        assertEquals((results[2] as Result.Error).error?.message, errorMessage, "Error message should match expected message")

        verify(apiService).sendLogout(check { it.endsWith("/api/v1/auth/logout") }, eq(xSession))
    }

    @Test
    fun `sendLogout should emit Loading and Error states when network error occurs`() = runTest {
        // Arrange
        val serverUrl = "http://test.com"
        val xSession = "testSession"
        val networkErrorMessage = "Network error"
        val ioException = IOException(networkErrorMessage)

        `when`(apiService.sendLogout(anyString(), anyString())).thenAnswer { invocation ->
            throw ioException
        }

        `when`(errorHandler.getError(ioException)).thenReturn(Result.ErrorType.IOError(ioException))

        // Act
        val results = authRepository.sendLogout(serverUrl, xSession).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null)
        assertTrue(results[1] is Result.Loading && results[1].data == "")
        assertTrue(results[2] is Result.Error && (results[2] as Result.Error).error is Result.ErrorType.IOError)
        assertEquals(networkErrorMessage, (results[2] as Result.Error).error?.throwable?.message)

        verify(apiService).sendLogout(check { it.endsWith("/api/v1/auth/logout") }, eq(xSession))
    }

    @Test
    fun `sendLoginV1 should emit Loading and Success states when API call is successful`() = runTest {
        // Arrange
        val username = "testUser"
        val password = "testPassword"
        val serverUrl = "http://test.com"
        val loginResponseMessageDTO = LoginResponseMessageDTO(
            expires = null,
            session = null,
            token = "testToken"
        )
        val loginResponseDTO = LoginResponseDTO(
            ok = true,
            message = loginResponseMessageDTO,
            error = null
        )
        val expectedUser =
            User("testToken", "testSession", Account(1, username, password, false, serverUrl))

        `when`(apiService.sendLoginV1(anyString(), any())).thenReturn(Response.success(loginResponseDTO))
        `when`(settingsPreferenceDataSource.getUser()).thenReturn(flowOf(expectedUser))

        // Act
        val results = authRepository.sendLoginV1(username, password, serverUrl).toList()

        // Assert
        assertEquals(3, results.size, "Expected 3 emitted results")
        assertTrue(results[0] is Result.Loading && results[0].data == null)
        assertTrue(results[1] is Result.Loading && results[1].data != null)
        assertTrue(results[2] is Result.Success && results[2].data == expectedUser)

        verify(settingsPreferenceDataSource).saveUser(any(), eq(serverUrl), eq(password))
        verify(apiService).sendLoginV1(check { it.endsWith("/api/v1/auth/login") }, any())
    }

    @Test
    fun `refreshToken should persist the new token and emit it`() = runTest {
        // Arrange
        val serverUrl = "http://test.com"
        val oldToken = "oldToken"
        val refreshResponse = LoginResponseDTO(
            ok = true,
            message = LoginResponseMessageDTO(expires = null, session = null, token = "newToken"),
            error = null
        )

        `when`(apiService.refreshToken(anyString(), anyString())).thenReturn(Response.success(refreshResponse))

        // Act
        val results = authRepository.refreshToken(serverUrl, oldToken).toList()

        // Assert
        assertEquals(2, results.size, "Expected 2 emitted results")
        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success && results[1].data == "newToken")

        verify(settingsPreferenceDataSource).updateAuthToken("newToken")
        verify(apiService).refreshToken(
            check { it.endsWith("/api/v1/auth/refresh") },
            eq("Bearer $oldToken")
        )
    }

    @Test
    fun `refreshToken should emit Error and keep the stored token when the server rejects it`() = runTest {
        // Arrange
        val serverUrl = "http://test.com"
        val errorBody = "Token not provided/invalid".toResponseBody("text/plain".toMediaTypeOrNull())

        `when`(apiService.refreshToken(anyString(), anyString()))
            .thenReturn(Response.error(403, errorBody))
        `when`(errorHandler.getApiError(eq(403), anyOrNull(), anyOrNull()))
            .thenReturn(Result.ErrorType.HttpError(statusCode = 403))

        // Act
        val results = authRepository.refreshToken(serverUrl, "expiredToken").toList()

        // Assert
        assertEquals(2, results.size, "Expected 2 emitted results")
        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Error)
        assertEquals(403, ((results[1] as Result.Error).error as Result.ErrorType.HttpError).statusCode)

        verify(settingsPreferenceDataSource, never()).updateAuthToken(anyString())
    }

    @Test
    fun `refreshToken should emit Error when the response carries no token`() = runTest {
        // Arrange
        val emptyResponse = LoginResponseDTO(
            ok = true,
            message = LoginResponseMessageDTO(expires = null, session = null, token = null),
            error = null
        )

        `when`(apiService.refreshToken(anyString(), anyString())).thenReturn(Response.success(emptyResponse))
        `when`(errorHandler.getError(any())).thenReturn(Result.ErrorType.Unknown())

        // Act
        val results = authRepository.refreshToken("http://test.com", "oldToken").toList()

        // Assert
        assertTrue(results.last() is Result.Error)
        verify(settingsPreferenceDataSource, never()).updateAuthToken(anyString())
    }
}
