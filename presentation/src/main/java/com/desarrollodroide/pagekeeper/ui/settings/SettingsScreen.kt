package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.data.helpers.SHIORI_GITHUB_URL
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.ui.components.ErrorDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import com.desarrollodroide.pagekeeper.ui.components.ContentMaxWidth
import kotlinx.coroutines.flow.MutableStateFlow
import com.desarrollodroide.pagekeeper.BuildConfig
import com.desarrollodroide.pagekeeper.extensions.sendFeedbackEmail
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onViewLastCrash: () -> Unit,
    onNavigateToTags: () -> Unit,
    goToLogin: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    val settingsUiState by settingsViewModel.settingsUiState.collectAsStateWithLifecycle()
    val tagsUiState by settingsViewModel.tagsState.collectAsStateWithLifecycle()
    val tagToHide by settingsViewModel.tagToHide.collectAsStateWithLifecycle()
    val compactView by settingsViewModel.compactView.collectAsStateWithLifecycle()
    val makeArchivePublic by settingsViewModel.makeArchivePublic.collectAsStateWithLifecycle()
    val createEbook by settingsViewModel.createEbook.collectAsStateWithLifecycle()
    val autoAddBookmark by settingsViewModel.autoAddBookmark.collectAsStateWithLifecycle()
    val serverVersion by settingsViewModel.serverVersion.collectAsStateWithLifecycle()
    val serverUrl by settingsViewModel.serverUrl.collectAsStateWithLifecycle()
    val createArchive by settingsViewModel.createArchive.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            SettingsContent(
                settingsUiState = settingsUiState,
                tagsUiState = tagsUiState,
                onLogout = { settingsViewModel.logout() },
                goToLogin = {
                    settingsViewModel.clearImageCache()
                    goToLogin.invoke()
                },
                themeMode = settingsViewModel.themeMode,
                makeArchivePublic = makeArchivePublic,
                onMakeArchivePublicChanged = { isPublic ->
                    settingsViewModel.setMakeArchivePublic(isPublic)
                },
                createEbook = createEbook,
                onCreateEbookChanged = { isEbook ->
                    settingsViewModel.setCreateEbook(isEbook)
                },
                createArchive = createArchive,
                onCreateArchiveChanged = { isArchive ->
                    settingsViewModel.setCreateArchive(isArchive)
                },
                compactView = compactView,
                onCompactViewChanged = { isCompact ->
                    settingsViewModel.setCompactView(isCompact)
                },
                autoAddBookmark = autoAddBookmark,
                onAutoAddBookmarkChanged = { isAuto ->
                    settingsViewModel.setAutoAddBookmark(isAuto)
                },
                onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onNavigateToSourceCode = onNavigateToSourceCode,
                onNavigateToLogs = onNavigateToLogs,
                onViewLastCrash = onViewLastCrash,
                onNavigateToTags = onNavigateToTags,
                useDynamicColors = settingsViewModel.useDynamicColors,
                onClickHideDialogOption = settingsViewModel::getTags,
                onHideTagChanged = settingsViewModel::setHideTag,
                hideTag = tagToHide,
                cacheSize = settingsViewModel.cacheSize,
                onClearCache = settingsViewModel::clearImageCache,
                serverVersion = serverVersion,
                serverUrl = serverUrl
            )
        }
    }
}

@Composable
fun SettingsContent(
    settingsUiState: UiState<String>,
    makeArchivePublic: Boolean,
    onMakeArchivePublicChanged: (Boolean) -> Unit,
    createEbook: Boolean,
    onCreateEbookChanged: (Boolean) -> Unit,
    createArchive: Boolean,
    onCreateArchiveChanged: (Boolean) -> Unit,
    autoAddBookmark: Boolean,
    onAutoAddBookmarkChanged: (Boolean) -> Unit,
    compactView: Boolean,
    onCompactViewChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onViewLastCrash: () -> Unit,
    onNavigateToTags: () -> Unit,
    themeMode: MutableStateFlow<ThemeMode>,
    goToLogin: () -> Unit,
    useDynamicColors: MutableStateFlow<Boolean>,
    tagsUiState: UiState<List<Tag>>,
    onClickHideDialogOption: () -> Unit,
    onHideTagChanged: (Tag?) -> Unit,
    hideTag: Tag?,
    cacheSize: StateFlow<String>,
    onClearCache: () -> Unit,
    serverVersion: String,
    serverUrl: String,
    ) {
    val context = LocalContext.current
    if (settingsUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
        Log.v("SettingsContent!!", "settingsUiState.isLoading")
    }
    if (!settingsUiState.error.isNullOrEmpty()) {
        ErrorDialog(
            title = "Error",
            content = settingsUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                goToLogin()
            }
        )
        Log.v("SettingsContent!!", settingsUiState.error)
    } else if (settingsUiState.data == null) {
        Log.v("SettingsContent!!", "settingsUiState.data is null")
    } else {
        Log.v("SettingsContent!!", "settingsUiState.data is not null")
        LaunchedEffect(Unit) {
            goToLogin()
        }
    }
    LazyColumn(
        // Centred and capped: settings rows stretched the full 1280dp of a tablet otherwise.
        modifier = Modifier
            .widthIn(max = ContentMaxWidth)
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            VisualSection(
                themeMode = themeMode,
                dynamicColors = useDynamicColors
            )
        }
        item {
            FeedSection(
                compactView = compactView,
                onCompactViewChanged = onCompactViewChanged,
                tagsUiState = tagsUiState,
                onHideTagChanged = onHideTagChanged,
                onClickHideDialogOption = onClickHideDialogOption,
                hideTag = hideTag,
                onNavigateToTags = onNavigateToTags
            )
        }
        item {
            DefaultsSection(
                makeArchivePublic = makeArchivePublic,
                onMakeArchivePublicChanged = onMakeArchivePublicChanged,
                createEbook = createEbook,
                onCreateEbookChanged = onCreateEbookChanged,
                createArchive = createArchive,
                onCreateArchiveChanged = onCreateArchiveChanged,
                autoAddBookmark = autoAddBookmark,
                onAutoAddBookmarkChanged = onAutoAddBookmarkChanged
            )
        }
        item {
            DataSection(
                cacheSize = cacheSize,
                onClearCache = onClearCache
            )
        }
        if (BuildConfig.FLAVOR == "staging") {
            item {
                DebugSection(
                    onNavigateToLogs = onNavigateToLogs,
                    onViewLastCrash = onViewLastCrash
                )
            }
        }
        item {
            AccountSection(
                serverUrl = serverUrl,
                onLogout = onLogout,
                onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onNavigateToSeverSettings = {
                    context.openUrlInBrowser(SHIORI_GITHUB_URL)
                },
                onSendFeedbackEmail = {
                    context.sendFeedbackEmail()
                },
                onNavigateToSourceCode = onNavigateToSourceCode
            )
        }
        item {
            VersionFooter(serverVersion = serverVersion)
        }
    }
}

/** Server + app version, shown once at the bottom of the settings list. */
@Composable
private fun VersionFooter(serverVersion: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (serverVersion.isNotEmpty()) {
            VersionLabel(
                icon = Icons.Default.Storage,
                text = "Server v$serverVersion",
                contentDescription = "Server version",
            )
        } else {
            Spacer(modifier = Modifier.width(0.dp))
        }
        VersionLabel(
            icon = Icons.Default.Smartphone,
            text = "App v${BuildConfig.VERSION_NAME}",
            contentDescription = "App version",
        )
    }
}

@Composable
private fun VersionLabel(
    icon: ImageVector,
    text: String,
    contentDescription: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class Item(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = "",
    val onClick: () -> Unit = {},
    val switchState: MutableStateFlow<Boolean> = MutableStateFlow(false)
)


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        settingsUiState = UiState(isLoading = false),
        makeArchivePublic = false,
        onMakeArchivePublicChanged = {},
        createEbook = false,
        onCreateEbookChanged = {},
        createArchive = false,
        onCreateArchiveChanged = {},
        autoAddBookmark = false,
        onAutoAddBookmarkChanged = { },
        compactView = false,
        onCompactViewChanged = {},
        onLogout = {},
        onNavigateToSourceCode = {},
        onNavigateToTermsOfUse = {},
        onNavigateToPrivacyPolicy = {},
        onNavigateToLogs = {},
        onViewLastCrash = {},
        onNavigateToTags = {},
        themeMode = remember { MutableStateFlow(ThemeMode.AUTO)},
        goToLogin = {},
        useDynamicColors = remember { MutableStateFlow(false) },
        tagsUiState = UiState(isLoading = false),
        onClickHideDialogOption = {},
        onHideTagChanged = {},
        hideTag = null,
        cacheSize = MutableStateFlow("Calculating..."),
        onClearCache = {},
        serverVersion = "1.0.0",
        serverUrl = "192.168.1.66:8888"
    )
}