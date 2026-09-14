package com.desarrollodroide.pagekeeper.ui.login

import androidx.lifecycle.SavedStateHandle
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

/**
 * What was typed on the login screen survives Android killing the app in the background.
 *
 * The form lived in plain state inside the view model, which dies with the process: switching to a
 * password manager to copy the password could come back to an empty server url and username. Seen
 * on a device with a process kill. The password itself is deliberately never kept.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginFormSavedStateTest {

    private val dispatcher = StandardTestDispatcher()

    private val preferences: SettingsPreferenceDataSource = mock<SettingsPreferenceDataSource>().stub {
        on { getUser() } doReturn flowOf(User(session = "", token = "", account = Account()))
        on { getRememberUser() } doReturn flowOf(Account())
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(handle: SavedStateHandle) = LoginViewModel(
        settingsPreferenceDataSource = preferences,
        loginUseCase = mock(),
        refreshTokenUseCase = mock(),
        livenessUseCase = mock(),
        savedStateHandle = handle,
    )

    @Test
    fun `the server url, username and remember me come back after process death`() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        viewModel(handle).apply {
            serverUrl.value = "http://ds224.example:18080"
            userName.value = "qa_user"
            rememberSession.value = true
        }

        val restored = viewModel(handle)
        testScheduler.advanceUntilIdle()

        assertEquals("http://ds224.example:18080", restored.serverUrl.value)
        assertEquals("qa_user", restored.userName.value)
        assertEquals(true, restored.rememberSession.value)
    }

    /** The pair (R7): the password is typed again, never kept. */
    @Test
    fun `the password does not come back`() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        viewModel(handle).password.value = "not-a-real-password"

        val restored = viewModel(handle)
        testScheduler.advanceUntilIdle()

        assertEquals("", restored.password.value)
    }
}
