package com.desarrollodroide.pagekeeper.ui.settings

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.data.helpers.SHIORI_GITHUB_URL
import com.desarrollodroide.data.helpers.ThemeMode
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser
import com.desarrollodroide.pagekeeper.ui.components.ErrorDialog
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import com.desarrollodroide.pagekeeper.BuildConfig
import com.desarrollodroide.pagekeeper.extensions.sendFeedbackEmail
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
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
    val createArchive by settingsViewModel.createArchive.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            SettingsContent(
                settingsUiState = settingsUiState,
                tagsUiState = tagsUiState,
                onLogout = { settingsViewModel.logout() },
                goToLogin = goToLogin,
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
                onCreateArchive = { isArchive ->
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
                useDynamicColors = settingsViewModel.useDynamicColors,
                onClickHideDialogOption = settingsViewModel::getTags,
                onSelectHideDialogOption = settingsViewModel::setHideTag,
                hideTag = tagToHide,
                cacheSize = settingsViewModel.cacheSize,
                onClearCache = settingsViewModel::clearImageCache
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
    onCreateArchive: (Boolean) -> Unit,
    autoAddBookmark: Boolean,
    onAutoAddBookmarkChanged: (Boolean) -> Unit,
    compactView: Boolean,
    onCompactViewChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    themeMode: MutableStateFlow<ThemeMode>,
    goToLogin: () -> Unit,
    useDynamicColors: MutableStateFlow<Boolean>,
    tagsUiState: UiState<List<Tag>>,
    onClickHideDialogOption: () -> Unit,
    onSelectHideDialogOption: (Tag?) -> Unit,
    hideTag: Tag?,
    cacheSize: StateFlow<String>,
    onClearCache: () -> Unit,
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
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            VisualSection(
                themeMode = themeMode,
                dynamicColors = useDynamicColors
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))
            FeedSection(
                compactView = compactView,
                onCompactViewChanged = onCompactViewChanged,
                tagsUiState = tagsUiState,
                onSelectHideDialogOption = onSelectHideDialogOption,
                onClickHideDialogOption = onClickHideDialogOption,
                hideTag = hideTag
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))
            DefaultsSection(
                makeArchivePublic = makeArchivePublic,
                onMakeArchivePublicChanged = onMakeArchivePublicChanged,
                createEbook = createEbook,
                onCreateEbookChanged = onCreateEbookChanged,
                createArchive = createArchive,
                onCreateArchive = onCreateArchive,
                autoAddBookmark = autoAddBookmark,
                onAutoAddBookmarkChanged = onAutoAddBookmarkChanged
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))
            DataSection(
                cacheSize = cacheSize,
                onClearCache = onClearCache
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))
            AccountSection(
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
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun HorizontalDivider(){
    HorizontalDivider(
        modifier = Modifier
            .height(1.dp)
            .padding(horizontal = 6.dp,),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    )
}

data class Item(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = "",
    val onClick: () -> Unit = {},
    val switchState: MutableStateFlow<Boolean> = MutableStateFlow(false)
)

data class Item2(
    val title: String,
    val icon: ImageVector,
    val subtitle: String = "",
    val isChecked: Boolean = false,
    val onCheckedChange: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null
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
        autoAddBookmark = false,
        compactView = false,
        onCompactViewChanged = {},
        onLogout = {},
        onNavigateToSourceCode = {},
        onNavigateToTermsOfUse = {},
        onNavigateToPrivacyPolicy = {},
        themeMode = remember { MutableStateFlow(ThemeMode.AUTO)},
        goToLogin = {},
        useDynamicColors = remember { MutableStateFlow(false) },
        tagsUiState = UiState(isLoading = false),
        onClickHideDialogOption = {},
        onSelectHideDialogOption = {},
        hideTag = null,
        cacheSize = MutableStateFlow("Calculating..."),
        onClearCache = {},
        onAutoAddBookmarkChanged = { },
        onCreateArchive = {}
    )
}