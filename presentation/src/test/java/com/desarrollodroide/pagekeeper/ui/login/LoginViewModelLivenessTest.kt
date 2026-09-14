package com.desarrollodroide.pagekeeper.ui.login

import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.domain.usecase.RefreshTokenUseCase
import com.desarrollodroide.domain.usecase.SendLoginUseCase
import com.desarrollodroide.domain.usecase.SystemLivenessUseCase
import com.desarrollodroide.model.Account
import com.desarrollodroide.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

/**
 * Log in first asks /system/liveness whether the server is there, and the spinner over the form
 * lasts until that answer is handled.
 *
 * Only a 404 (a server older than 1.6) and an IO error used to be handled. Anything else — a 401
 * from a reverse proxy, a 502 from one whose Shiori is down, a 500 — fell through every branch,
 * left the state loading, and the dialog cannot be dismissed: the only way out was to kill the
 * app. Seen on a device against a server answering 500.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelLivenessTest {

    private val dispatcher = StandardTestDispatcher()

    private val preferences: SettingsPreferenceDataSource = mock<SettingsPreferenceDataSource>().stub {
        on { getUser() } doReturn flowOf(User(session = "", token = "", account = Account()))
        on { getRememberUser() } doReturn flowOf(Account())
    }
    private val loginUseCase: SendLoginUseCase = mock<SendLoginUseCase>().stub {
        on { invoke(any(), any(), any()) } doReturn emptyFlow()
    }
    private val livenessUseCase: SystemLivenessUseCase = mock()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = LoginViewModel(
        settingsPreferenceDataSource = preferences,
        loginUseCase = loginUseCase,
        refreshTokenUseCase = mock<RefreshTokenUseCase>(),
        livenessUseCase = livenessUseCase,
    )

    private fun livenessAnswers(error: Result.ErrorType) = livenessUseCase.stub {
        on { invoke(any()) } doReturn flowOf(Result.Loading(null), Result.Error(error))
    }

    @ParameterizedTest
    @ValueSource(ints = [401, 403, 500, 502, 503])
    fun `an http error other than 404 ends the spinner with an error`(code: Int) = runTest(dispatcher) {
        livenessAnswers(Result.ErrorType.HttpError(statusCode = code, message = "HTTP $code"))
        val vm = viewModel()

        vm.checkSystemLiveness()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.livenessUiState.value.isLoading, "still loading after HTTP $code")
        assertNotNull(vm.livenessUiState.value.error, "no error shown for HTTP $code")
        verify(loginUseCase, never()).invoke(any(), any(), any())
    }

    @Test
    fun `an unknown error ends the spinner with an error`() = runTest(dispatcher) {
        livenessAnswers(Result.ErrorType.Unknown(IllegalStateException("not json")))
        val vm = viewModel()

        vm.checkSystemLiveness()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.livenessUiState.value.isLoading)
        assertNotNull(vm.livenessUiState.value.error)
    }

    /** The pair (R7): a 404 is not an error, it is an old server, and the login goes ahead. */
    @Test
    fun `a 404 is an old server and the login goes ahead without an error`() = runTest(dispatcher) {
        livenessAnswers(Result.ErrorType.HttpError(statusCode = 404, message = "not found"))
        val vm = viewModel()

        vm.checkSystemLiveness()
        testScheduler.advanceUntilIdle()

        assertNull(vm.livenessUiState.value.error)
        verify(loginUseCase).invoke(any(), any(), any())
    }
}
