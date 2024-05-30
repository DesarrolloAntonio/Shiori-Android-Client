package com.desarrollodroide.pagekeeper.ui.readablecontent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.domain.usecase.GetBookmarkReadableContentUseCase
import com.desarrollodroide.model.ReadableMessage
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ReadableContentViewModel(
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
    private val getBookmarkReadableContentUseCase: GetBookmarkReadableContentUseCase
) : ViewModel() {

    private var serverUrl = ""
    private var token = ""

    private val _readableContentState = MutableStateFlow(UiState<ReadableMessage>(idle = true))
    val readableContentState = _readableContentState.asStateFlow()

    val themeMode = MutableStateFlow<ThemeMode>(ThemeMode.AUTO)


    fun loadInitialData() {
        viewModelScope.launch {
            serverUrl = settingsPreferenceDataSource.getUrl()
            token = settingsPreferenceDataSource.getToken()
            themeMode.value = settingsPreferenceDataSource.getThemeMode()
        }
    }

    fun getBookmarkReadableContent(
        bookmarkId: Int
    ) {
        viewModelScope.launch {
            getBookmarkReadableContentUseCase.invoke(
                serverUrl = serverUrl,
                token = token,
                bookmarkId = bookmarkId
            )
                .distinctUntilChanged()
                .collect() { result ->
                    when (result) {
                        is Result.Error -> {
                            Log.v(
                                "ReadableContent",
                                "Error getting bookmark readable content: ${result.error?.message}"
                            )
                        }
                        is Result.Loading -> {
                            Log.v(
                                "ReadableContent",
                                "Loading, getting bookmark readable content..."
                            )
                            _readableContentState.isLoading(true)
                        }

                        is Result.Success -> {
                            Log.v("ReadableContent", "Get bookmark readable content successfully.")
                            result.data?.let {
                                _readableContentState.success(it.message)
                            }
                        }
                        else -> {}
                    }
                }
        }
    }
}