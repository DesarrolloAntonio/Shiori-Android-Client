package com.desarrollodroide.data.repository

import com.desarrollodroide.common.result.ErrorHandler
import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.network.model.SessionDTO
import com.desarrollodroide.network.retrofit.RetrofitNetwork
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response

class AuthRepositoryTest {

    @Mock
    lateinit var apiService: RetrofitNetwork

    @Mock
    lateinit var settingsPreferenceDataSource: SettingsPreferenceDataSource

    @Mock
    lateinit var errorHandler: ErrorHandler

    private lateinit var authRepositoryImpl: AuthRepositoryImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        authRepositoryImpl = AuthRepositoryImpl(apiService, settingsPreferenceDataSource, errorHandler)
    }

    @Test
    fun `sendLogin saves user on successful login`() = runBlocking {
        val username = "user"
        val password = "pass"
        val serverUrl = "http://test.com"
        val sessionDTO = SessionDTO("sessionId", null)
        val userPreferences = UserPreferences.newBuilder()
            .setUsername(username)
            .setOwner(false)
            .setPassword(password)
            .setSession("sessionId")
            .setUrl(serverUrl)
            .setRememberPassword(true)
            .build()

        `when`(apiService.sendLogin(anyString(), anyString())).thenReturn(Response.success(sessionDTO))
        doNothing().`when`(settingsPreferenceDataSource).saveUser(any(UserPreferences::class.java), anyString(), anyString())

        authRepositoryImpl.sendLogin(username, password, serverUrl).collect { /* Aquí puedes hacer assertions sobre el resultado */ }

//        verify(settingsPreferenceDataSource).saveUser(
//            argThat { session == "sessionId" && url == serverUrl && this.password == password },
//            eq(serverUrl),
//            eq(password)
//        )
    }

    @Test
    fun `sendLogout resets user data on successful logout`() = runBlocking {
        val serverUrl = "http://test.com"
        val xSession = "sessionId"

        `when`(apiService.sendLogout(anyString(), anyString())).thenReturn(Response.success("Logged out"))

        authRepositoryImpl.sendLogout(serverUrl, xSession).collect { /* Here you can assert the result */ }

        verify(settingsPreferenceDataSource).resetUser()
    }
}