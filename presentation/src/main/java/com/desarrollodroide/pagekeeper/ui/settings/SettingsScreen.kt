package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.ui.text.style.TextAlign
import com.desarrollodroide.pagekeeper.ui.components.DialogDismissButton
import com.desarrollodroide.pagekeeper.ui.components.DialogConfirmButton
import com.desarrollodroide.pagekeeper.ui.components.ShioriAlertDialog
import com.desarrollodroide.pagekeeper.ui.components.DialogTone
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.toShape
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
            SettingsHeader(onBack = onBack, scrollBehavior = scrollBehavior)
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        // The account first: which server this is, and the way out. Then the app's own settings,
        // most used first, and the small print last.
        item {
            AccountSection(
                serverUrl = serverUrl,
                onLogout = onLogout,
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
            VisualSection(
                themeMode = themeMode,
                dynamicColors = useDynamicColors
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
            AboutSection(
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
    ShioriAlertDialog(
        onDismissRequest = onDismiss,
        icon = Icons.AutoMirrored.Filled.Logout,
        tone = DialogTone.Danger,
        title = "Log out?",
        text = {
            Text(
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                text = if (pendingChanges > 0) {
                    val changes = if (pendingChanges == 1) "1 change has" else "$pendingChanges changes have"
                    "$changes not reached the server yet and will be lost. Bookmarks on the server are not affected."
                } else {
                    "Bookmarks stored on this device will be removed. Bookmarks on the server are not affected."
                },
            )
        },
        // Log out is the destructive one: it empties this device's copy.
        confirmButton = {
            DialogConfirmButton(text = "Log out", onClick = onConfirm, destructive = true)
        },
        dismissButton = { DialogDismissButton(text = "Cancel", onClick = onDismiss) },
    )
}

/** App and server version, once, at the bottom of the list. */
@Composable
private fun VersionFooter(serverVersion: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        VersionPill(
            icon = Icons.Default.Smartphone,
            text = "App v${BuildConfig.VERSION_NAME}",
            contentDescription = "App version",
        )
        if (serverVersion.isNotEmpty()) {
            VersionPill(
                icon = Icons.Default.Storage,
                text = "Server v$serverVersion",
                contentDescription = "Server version",
            )
        }
    }
}

@Composable
private fun VersionPill(
    icon: ImageVector,
    text: String,
    contentDescription: String,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * "Settings" set large over a coral flower, collapsing as the list scrolls the way the library and
 * Tags headers do: the big word shrinks away and a small one takes its place beside Back.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsHeader(
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val state = scrollBehavior.state
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { SettingsTitleHeight.toPx() }
    SideEffect {
        if (state.heightOffsetLimit != -expandedHeightPx) {
            state.heightOffsetLimit = -expandedHeightPx
        }
    }
    val collapsedFraction = state.collapsedFraction
    val titleHeight = with(density) { (expandedHeightPx + state.heightOffset).coerceAtLeast(0f).toDp() }
    val statusBarTop = WindowInsets.statusBars.getTop(density).toFloat()
    val flower = MaterialShapes.Flower.toShape()
    val sun = MaterialShapes.Sunny.toShape()
    val flowerColor = MaterialTheme.colorScheme.tertiaryContainer
    val sunColor = MaterialTheme.colorScheme.secondaryContainer

    Surface(color = lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer, collapsedFraction)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                // Drawn rather than laid out, so they take no space, and they fade as it collapses.
                .drawBehind {
                    val alpha = 1f - collapsedFraction
                    val flowerSide = 280.dp.toPx()
                    translate(left = size.width - flowerSide + 100.dp.toPx(), top = statusBarTop - 110.dp.toPx()) {
                        drawOutline(flower.createOutline(Size(flowerSide, flowerSide), layoutDirection, this), flowerColor, alpha = alpha)
                    }
                    val sunSide = 40.dp.toPx()
                    translate(left = size.width - sunSide - 30.dp.toPx(), top = statusBarTop + 96.dp.toPx()) {
                        drawOutline(sun.createOutline(Size(sunSide, sunSide), layoutDirection, this), sunColor, alpha = alpha)
                    }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Settings",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .graphicsLayer { alpha = collapsedFraction },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(titleHeight)
                        .clipToBounds(),
                ) {
                    Text(
                        text = "Settings",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                            .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                            .graphicsLayer { alpha = 1f - collapsedFraction }
                            .semantics { heading() },
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 60.sp,
                            lineHeight = 64.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-2.5).sp,
                        ),
                    )
                }
            }
        }
    }
}

private val SettingsTitleHeight = 76.dp

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