package com.desarrollodroide.pagekeeper.ui.tags

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.common.result.Result
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import com.desarrollodroide.domain.usecase.CreateTagUseCase
import com.desarrollodroide.domain.usecase.DeleteTagUseCase
import com.desarrollodroide.domain.usecase.GetTagsUseCase
import com.desarrollodroide.domain.usecase.RenameTagUseCase
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.error
import com.desarrollodroide.pagekeeper.ui.components.isLoading
import com.desarrollodroide.pagekeeper.ui.components.success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagsViewModel(
    private val getTagsUseCase: GetTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val renameTagUseCase: RenameTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val settingsPreferenceDataSource: SettingsPreferenceDataSource,
) : ViewModel() {

    /**
     * The list is driven off the Room cache rather than the network result, so a create, rename or
     * delete shows up as soon as the repository has written it, without waiting for a refetch.
     */
    val tags = getTagsUseCase.getLocalTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _tagsUiState = MutableStateFlow(UiState<List<Tag>>(idle = true))
    val tagsUiState = _tagsUiState.asStateFlow()

    /** One-shot message for failures that should not blank the list, e.g. a rejected rename. */
    private val _actionError = MutableStateFlow<String?>(null)
    val actionError = _actionError.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            getTagsUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                token = settingsPreferenceDataSource.getToken(),
            ).collect { result ->
                when (result) {
                    is Result.Loading -> _tagsUiState.isLoading(true)
                    is Result.Success -> _tagsUiState.success(result.data)
                    is Result.Error -> {
                        Log.v(TAG, "Error loading tags: ${result.error?.message}")
                        _tagsUiState.error(result.error?.message ?: "Could not load tags")
                    }
                }
            }
        }
    }

    fun createTag(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            createTagUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                token = settingsPreferenceDataSource.getToken(),
                name = name,
            ).collect { it.reportIfFailed("Could not create tag") }
        }
    }

    fun renameTag(tag: Tag, newName: String) {
        if (newName.isBlank() || newName.trim() == tag.name) return
        viewModelScope.launch {
            renameTagUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                token = settingsPreferenceDataSource.getToken(),
                tagId = tag.id,
                name = newName,
            ).collect { it.reportIfFailed("Could not rename tag") }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            deleteTagUseCase(
                serverUrl = settingsPreferenceDataSource.getUrl(),
                token = settingsPreferenceDataSource.getToken(),
                tagId = tag.id,
            ).collect { it.reportIfFailed("Could not delete tag") }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }

    private fun <T> Result<T>.reportIfFailed(fallback: String) {
        if (this is Result.Error) {
            Log.v(TAG, "$fallback: ${error?.message}")
            _actionError.value = error?.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    private companion object {
        const val TAG = "TagsViewModel"
    }
}
