package com.desarrollodroide.pagekeeper.ui.settings

import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.UiState

sealed class SettingsSectionState {

    /**
     * Represents the visual settings section.
     */
    data class Visual(
        val themeMode: ThemeMode,
        val useDynamicColors: Boolean
    ) : SettingsSectionState()

    /**
     * Represents the feed settings section.
     */
    data class Feed(
        val compactView: Boolean,
        val hideTag: Tag?,
        val tagsUiState: UiState<List<Tag>>
    ) : SettingsSectionState()

    /**
     * Represents the defaults settings section.
     */
    data class Defaults(
        val makeArchivePublic: Boolean,
        val createEbook: Boolean,
        val createArchive: Boolean,
        val autoAddBookmark: Boolean
    ) : SettingsSectionState()

    /**
     * Represents the data settings section.
     */
    data class Data(
        val cacheSize: String
    ) : SettingsSectionState()

    /**
     * Represents an error state within the settings.
     */
    data class Error(val message: String) : SettingsSectionState()

    /**
     * Represents a loading state within the settings.
     */
    object Loading : SettingsSectionState()
}

