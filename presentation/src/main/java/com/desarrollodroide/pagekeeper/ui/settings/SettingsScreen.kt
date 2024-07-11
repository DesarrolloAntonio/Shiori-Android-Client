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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    val settingsUiState = settingsViewModel.settingsUiState.collectAsState().value
    val tagsUiState = settingsViewModel.tagsState.collectAsState().value
    val tagToHide = settingsViewModel.tagToHide.collectAsState().value

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
                makeArchivePublic = settingsViewModel.makeArchivePublic,
                createEbook = settingsViewModel.createEbook,
                createArchive = settingsViewModel.createArchive,
                compatView = settingsViewModel.compactView,
                onNavigateToTermsOfUse = onNavigateToTermsOfUse,
                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                onNavigateToSourceCode = onNavigateToSourceCode,
                useDynamicColors = settingsViewModel.useDynamicColors,
                onClickHideDialogOption = settingsViewModel::getTags,
                onSelectHideDialogOption = settingsViewModel::setHideTag,
                hideTag = tagToHide
            )
        }
    }
}

@Composable
fun SettingsContent(
    settingsUiState: UiState<String>,
    makeArchivePublic: MutableStateFlow<Boolean>,
    createEbook: MutableStateFlow<Boolean>,
    createArchive: MutableStateFlow<Boolean>,
    compatView: MutableStateFlow<Boolean>,
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
                compactView = compatView,
                tagsUiState = tagsUiState,
                onSelectHideDialogOption = onSelectHideDialogOption,
                onClickHideDialogOption = onClickHideDialogOption,
                hideTag = hideTag
            )
            HorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))
            DefaultsSection(
                makeArchivePublic = makeArchivePublic,
                createEbook = createEbook,
                createArchive = createArchive
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        settingsUiState = UiState(isLoading = false),
        makeArchivePublic = remember { MutableStateFlow(false) },
        createEbook = remember { MutableStateFlow(false) },
        createArchive = remember { MutableStateFlow(false) },
        compatView = remember { MutableStateFlow(false) },
        onLogout = {},
        onNavigateToSourceCode = {},
        onNavigateToTermsOfUse = {},
        onNavigateToPrivacyPolicy = {},
        //onCompactViewChanged = remember { MutableStateFlow(false) },
        themeMode = remember { MutableStateFlow(ThemeMode.AUTO)},
        goToLogin = {},
        useDynamicColors = remember { MutableStateFlow(false) },
        tagsUiState = UiState(isLoading = false),
        onClickHideDialogOption = {},
        onSelectHideDialogOption = {},
        hideTag = null,
    )
}