package com.desarrollodroide.domain.usecase

import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.data.repository.AuthRepository
import com.desarrollodroide.data.repository.SessionCheck
import com.desarrollodroide.data.repository.SyncWorks
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Issue #64: the server's address could only change by logging out, which drops the queue. The
 * new address is only written once it takes the stored session as the same user, because queued
 * edits and deletes name bookmarks by that server's ids.
 */
class ChangeServerUrlUseCaseTest {

    private val authRepository: AuthRepository = mock()
    private val settings: SettingsPreferenceDataSource = mock()
    private val syncWorks: SyncWorks = mock()
    private val useCase = ChangeServerUrlUseCase(authRepository, settings, syncWorks)

    @BeforeEach
    fun signedIn() {
        whenever(settings.getUser()).thenReturn(
            flowOf(
                User(
                    session = "token", token = "token",
                    account = Account(userName = "shiori", password = "", owner = true, serverUrl = "http://nas.ts.net:18080"),
                )
            )
        )
    }

    @Test
    fun `an address that takes the session is saved and the queue is sent again`() = runTest {
        whenever(authRepository.checkSession("http://192.168.1.68:18080", "token", "shiori")).thenReturn(SessionCheck.Accepted)

        val result = useCase("  http://192.168.1.68:18080/ ")

        assertEquals(SessionCheck.Accepted, result)
        verify(settings).updateServerUrl("http://192.168.1.68:18080")
        verify(syncWorks).retryAllPendingJobs()
    }

    /** The pair (R7): anything short of the same account leaves the address and the queue alone. */
    @Test
    fun `an address that does not take the session changes nothing`() = runTest {
        listOf(SessionCheck.Refused, SessionCheck.NotShiori, SessionCheck.Unreachable("timeout")).forEach { answer ->
            whenever(authRepository.checkSession(any(), any(), any())).thenReturn(answer)

            assertEquals(answer, useCase("http://other.example"))
        }

        verify(settings, never()).updateServerUrl(any())
        verify(syncWorks, never()).retryAllPendingJobs()
    }
}
