package com.desarrollodroide.pagekeeper.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
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
    val logoutUiState by settingsViewModel.logoutUiState.collectAsStateWithLifecycle()
    val tagsUiState by settingsViewModel.tagsState.collectAsStateWithLifecycle()
    val tagToHide by settingsViewModel.tagToHide.collectAsStateWithLifecycle()
    val compactView by settingsViewModel.compactView.collectAsStateWithLifecycle()
    val useTwoPaneLayout by settingsViewModel.useTwoPaneLayout.collectAsStateWithLifecycle()
    val makeArchivePublic by settingsViewModel.makeArchivePublic.collectAsStateWithLifecycle()
    val createEbook by settingsViewModel.createEbook.collectAsStateWithLifecycle()
    val autoAddBookmark by settingsViewModel.autoAddBookmark.collectAsStateWithLifecycle()
    val serverVersion by settingsViewModel.serverVersion.collectAsStateWithLifecycle()
    val serverUrl by settingsViewModel.serverUrl.collectAsStateWithLifecycle()
    val createArchive by settingsViewModel.createArchive.collectAsStateWithLifecycle()
    val logoutConfirmation by settingsViewModel.logoutConfirmation.collectAsStateWithLifecycle()

    logoutConfirmation?.let { pendingChanges ->
        LogoutConfirmationDialog(
            pendingChanges = pendingChanges,
            onConfirm = settingsViewModel::confirmLogout,
            onDismiss = settingsViewModel::cancelLogout,
        )
    }

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
                logoutUiState = logoutUiState,
                tagsUiState = tagsUiState,
                onLogout = { settingsViewModel.requestLogout() },
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
                useTwoPaneLayout = useTwoPaneLayout,
                onUseTwoPaneLayoutChanged = { useTwoPane ->
                    settingsViewModel.setUseTwoPaneLayout(useTwoPane)
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
    logoutUiState: UiState<String>,
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
    useTwoPaneLayout: Boolean,
    onUseTwoPaneLayoutChanged: (Boolean) -> Unit,
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
    // The only operation this state tracks is the logout. Null data is the resting state, not an
    // absent screen: it means nobody has pressed Log out yet. Data means the server said goodbye,
    // so the session is gone and there is nothing left here to show.
    if (logoutUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }
    if (!logoutUiState.error.isNullOrEmpty()) {
        ErrorDialog(
            title = "Error",
            content = logoutUiState.error,
            openDialog = remember { mutableStateOf(true) },
            onConfirm = {
                goToLogin()
            },
            // Accept is the only way out: it is what leaves Settings, and the session is already
            // gone. Dismissing by Back or outside used to leave a signed-out user here.
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        )
    } else if (logoutUiState.data != null) {
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
                useTwoPaneLayout = useTwoPaneLayout,
                onUseTwoPaneLayoutChanged = onUseTwoPaneLayoutChanged,
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

/**
 * Logout empties the local database and cancels the sync queue, so it asks first, as the web UI
 * does. When changes are still waiting to reach the server it says they will be lost: offline,
 * one tap used to discard a bookmark that had never been sent.
 */
@Composable
private fun LogoutConfirmationDialog(
    pendingChanges: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
        title = { Text("Log out?") },
        text = {
            Text(
                if (pendingChanges > 0) {
                    val changes = if (pendingChanges == 1) "1 change has" else "$pendingChanges changes have"
                    "$changes not reached the server yet and will be lost. Bookmarks on the server are not affected."
                } else {
                    "Bookmarks stored on this device will be removed. Bookmarks on the server are not affected."
                }
            )
        },
        // Log out is the destructive one: it empties this device's copy.
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Log out") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
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
private fun SettingsScreenPreview() {
    SettingsContent(
        logoutUiState = UiState(isLoading = false),
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
        useTwoPaneLayout = false,
        onUseTwoPaneLayoutChanged = {},
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