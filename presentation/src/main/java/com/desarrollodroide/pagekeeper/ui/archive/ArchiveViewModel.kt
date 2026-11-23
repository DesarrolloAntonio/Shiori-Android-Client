package com.desarrollodroide.pagekeeper.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.data.local.preferences.SettingsPreferenceDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** The server and session the archive viewer needs. Read here, so the token never travels in a route. */
class ArchiveViewModel(
    private val settings: SettingsPreferenceDataSource,
) : ViewModel() {

    data class Session(val serverUrl: String, val token: String)

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    init {
        viewModelScope.launch {
            val user = settings.getUser().first()
            _session.value = Session(serverUrl = user.account.serverUrl, token = user.token)
        }
    }
}
