package com.desarrollodroide.pagekeeper.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.desarrollodroide.data.helpers.SHIORI_ANDROID_CLIENT_GITHUB_URL
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.pagekeeper.navigation.NavItem
import com.desarrollodroide.pagekeeper.ui.feed.FeedScreen
import com.desarrollodroide.pagekeeper.ui.feed.FeedViewModel
import com.desarrollodroide.pagekeeper.ui.settings.PrivacyPolicyScreen
import com.desarrollodroide.pagekeeper.ui.settings.SettingsScreen
import com.desarrollodroide.pagekeeper.ui.tags.TagsScreen
import com.desarrollodroide.pagekeeper.ui.settings.TermsOfUseScreen
import java.io.File
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.pagekeeper.extensions.isRTLText
import com.desarrollodroide.pagekeeper.ui.readablecontent.ReadableContentScreen
import com.desarrollodroide.pagekeeper.ui.settings.crash.CrashLogScreen
import com.desarrollodroide.pagekeeper.ui.settings.logcat.NetworkLogScreen
import kotlinx.coroutines.launch
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.pagekeeper.ui.components.TwoPaneEmptyDetail
import com.desarrollodroide.pagekeeper.ui.components.shouldUseTwoPanes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    onFinish: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
    onAddManuallyClick: () -> Unit,
    shareEpubFile: (File) -> Unit,
    shareText: (String) -> Unit
) {
    val navController = rememberNavController()
    val isCategoriesVisible = remember { mutableStateOf(false) }
    val isSearchBarVisible = remember { mutableStateOf(false) }
    val (showTopBar, setShowTopBar) = remember { mutableStateOf(true) }
    val hasBookmarks = feedViewModel.bookmarksState.collectAsLazyPagingItems().itemCount > 0
    val selectedTags by feedViewModel.selectedTags.collectAsState()
    val showOnlyHiddenTag by feedViewModel.showOnlyHiddenTag.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false, confirmValueChange = { true })
    val showBottomSheet = remember { mutableStateOf(false) }

    BackHandler {
        onFinish()
    }

    val pendingJobs by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { showBottomSheet.value = false }
        ) {
            SyncJobsBottomSheetContent(
                pendingJobs = pendingJobs,
                onDismiss = { showBottomSheet.value = false },
                onRetryAll = { feedViewModel.retryAllPendingJobs() }
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavItem.HomeNavItem.route
    ) {
        composable(NavItem.HomeNavItem.route) {
            val pendingJobsCount by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            val pendingJobs by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    AnimatedVisibility (showTopBar) {
                        TopBar(
                            onSearchClick = { isSearchBarVisible.value = !isSearchBarVisible.value },
                            onFilterClick = { isCategoriesVisible.value = !isCategoriesVisible.value },
                            onSettingsClick = { navController.navigate(NavItem.SettingsNavItem.route) },
                            onSyncClick = {
                                coroutineScope.launch {
                                    showBottomSheet.value = true
                                    bottomSheetState.show()
                                }
                            },
                            selectedTagsCount = selectedTags.size,
                            showOnlyHiddenTag = showOnlyHiddenTag,
                            pendingJobsCount = pendingJobsCount.size,
                            pendingJobs = pendingJobs,
                        )
                    }
                },
                floatingActionButton = {
                    AnimatedVisibility(showTopBar) {
                        FloatingActionButton(
                            onClick = onAddManuallyClick,
                            shape = MaterialTheme.shapes.large,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add bookmark")
                        }
                    }
                }
            ) { paddingValues ->
                val useTwoPaneLayout by feedViewModel.useTwoPaneLayout.collectAsStateWithLifecycle()
                // Which bookmark the detail pane is showing. Only the id is kept: Bookmark is not
                // parcelable, and the feed view model can resolve it again after a rotation.
                var openBookmarkId by rememberSaveable { mutableStateOf<Int?>(null) }

                BoxWithConstraints(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    // Measured rather than taken from a window size class, so the same decision can
                    // be driven from a test by handing the composable a width.
                    val twoPanes = shouldUseTwoPanes(useTwoPaneLayout, maxWidth)

                    val feed: @Composable (Modifier) -> Unit = { _ ->
                        FeedScreen(
                            feedViewModel = feedViewModel,
                            isCategoriesVisible = isCategoriesVisible,
                            goToLogin = goToLogin,
                            openUrlInBrowser = openUrlInBrowser,
                            shareEpubFile = shareEpubFile,
                            isSearchBarVisible = isSearchBarVisible,
                            setShowTopBar = setShowTopBar,
                            goToReadableContent = { bookmark ->
                                if (twoPanes) {
                                    openBookmarkId = bookmark.id
                                } else {
                                    navController.navigate(
                                        NavItem.ReadableContentNavItem.createRoute(
                                            bookmarkId = bookmark.id,
                                        )
                                    )
                                }
                            },
                        )
                    }

                    if (twoPanes) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) { feed(Modifier) }
                            VerticalDivider()
                            Box(modifier = Modifier.weight(1f)) {
                                TwoPaneDetail(
                                    bookmarkId = openBookmarkId,
                                    feedViewModel = feedViewModel,
                                    openUrlInBrowser = openUrlInBrowser,
                                    onClose = { openBookmarkId = null },
                                )
                            }
                        }
                    } else {
                        // The pane can only have been opened on a wider window. Drop it, or the
                        // selection would come back on the next rotation into landscape.
                        LaunchedEffect(twoPanes) { openBookmarkId = null }
                        feed(Modifier)
                    }
                }
            }
        }
        composable(NavItem.SettingsNavItem.route) {
            SettingsScreen(
                settingsViewModel = koinViewModel(),
                goToLogin = goToLogin,
                onNavigateToPrivacyPolicy = {
                    navController.navigate(NavItem.PrivacyPolicyNavItem.route)
                },
                onNavigateToTermsOfUse = {
                    navController.navigate(NavItem.TermsOfUseNavItem.route)
                },
                onBack = {
                    navController.navigateUp()
                },
                onNavigateToSourceCode = {
                    openUrlInBrowser.invoke(SHIORI_ANDROID_CLIENT_GITHUB_URL)
                },
                onNavigateToLogs = {
                    navController.navigate(NavItem.NetworkLoggerNavItem.route)
                },
                onViewLastCrash = {
                    navController.navigate(NavItem.LastCrashNavItem.route)
                },
                onNavigateToTags = {
                    navController.navigate(NavItem.TagsNavItem.route)
                }
            )
        }
        composable(NavItem.TagsNavItem.route) {
            TagsScreen(
                tagsViewModel = koinViewModel(),
                onBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(NavItem.TermsOfUseNavItem.route) {
            TermsOfUseScreen(
                onBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(NavItem.PrivacyPolicyNavItem.route) {
            PrivacyPolicyScreen(
                onBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(NavItem.NetworkLoggerNavItem.route) {
            NetworkLogScreen(
                onBack = {
                    navController.navigateUp()
                },
                onShare = shareText
            )
        }
        composable(NavItem.LastCrashNavItem.route) {
            CrashLogScreen(
                onBack = {
                    navController.navigateUp()
                },
                onShare = shareText
            )
        }
        composable(
            route = NavItem.ReadableContentNavItem.route,
            arguments = listOf(
                navArgument("bookmarkId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val bookmarkId = backStackEntry.arguments?.getInt("bookmarkId") ?: 0
            val bookmark by feedViewModel.currentBookmark.collectAsState()

            LaunchedEffect(bookmarkId) {
                feedViewModel.loadBookmarkById(bookmarkId)
            }

            bookmark?.let {
                ReadableContentScreen(
                    readableContentViewModel = koinViewModel(),
                    bookmarkId = bookmarkId,
                    bookmarkUrl = it.url,
                    onBack = {
                        navController.navigateUp()
                    },
                    openUrlInBrowser = openUrlInBrowser,
                    bookmarkDate = it.modified,
                    bookmarkTitle = it.title,
                    isRtl = it.title.isRTLText() || it.excerpt.isRTLText()
                )
            }
        }
    }
}

/**
 * Search app bar.
 *
 * M3 names this variant for exactly this case: "use on home pages when search is key to the
 * product", with a search field in place of heading text, a product logo as the leading element
 * and at most two trailing icons. It replaces both the two row flexible bar and the separate
 * search icon, so the feed gets one 64dp row of chrome instead of roughly 230dp of bar top and
 * bottom.
 *
 * The guidance also says an app bar should carry one or two actions and that anything more
 * belongs elsewhere, which is what forced the choice here: filter and settings are the two, add
 * is the fab, and sync only appears when there is actually something pending.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSyncClick: () -> Unit,
    selectedTagsCount: Int,
    showOnlyHiddenTag: Boolean,
    pendingJobsCount: Int,
    pendingJobs: List<PendingJob>,
) {
    val hasRunningJobs = pendingJobs.any { it.state.uppercase() == "RUNNING" }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(hasRunningJobs) {
        if (hasRunningJobs) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.snapTo(0f)
        }
    }

    val hint = when {
        showOnlyHiddenTag -> "Hidden tags only"
        selectedTagsCount > 0 -> "$selectedTagsCount tag filter" + if (selectedTagsCount > 1) "s" else ""
        else -> "Search bookmarks"
    }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_pagekeeper),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(32.dp)
            )
            // The field is the headline. surfaceContainerHigh keeps it distinct from the app
            // background, which is what the spec asks for.
            Surface(
                onClick = onSearchClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            // Sync earns its place only while something is queued, so the bar stays at two
            // actions the rest of the time.
            AnimatedVisibility(visible = pendingJobsCount > 0) {
                BadgedIconButton(
                    onClick = onSyncClick,
                    badgeCount = pendingJobsCount,
                    description = "Pending sync",
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
                    )
                }
            }
            BadgedIconButton(
                onClick = onFilterClick,
                badgeCount = if (showOnlyHiddenTag) 0 else selectedTagsCount,
                description = if (showOnlyHiddenTag) "Hidden tags" else "Filter by tag",
            ) {
                Icon(
                    imageVector = if (showOnlyHiddenTag) Icons.Default.VisibilityOff else Icons.Outlined.Sell,
                    contentDescription = null,
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    }
}

/** Icon button that carries a count badge, animated in and out. */
@Composable
private fun BadgedIconButton(
    onClick: () -> Unit,
    badgeCount: Int,
    description: String,
    icon: @Composable () -> Unit,
) {
    BadgedBox(
        badge = {
            androidx.compose.animation.AnimatedVisibility(
                visible = badgeCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Badge {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    ) {
        // The description goes on the button, not the glyph, so TalkBack announces the action once
        // ("Sync") rather than describing the icon inside a separately-labelled clickable.
        IconButton(
            onClick = onClick,
            modifier = Modifier.semantics { this.contentDescription = description },
        ) {
            icon()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SyncJobsBottomSheetContent(
    pendingJobs: List<PendingJob>,
    onDismiss: () -> Unit,
    onRetryAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Pending sync jobs",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
        )
        if (pendingJobs.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                text = "Nothing waiting to sync",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column {
                    pendingJobs.forEach { job ->
                        val state = job.state.uppercase()
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            overlineContent = {
                                Text(
                                    text = job.operationType.name,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = job.bookmarkTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (state == "RUNNING") {
                                        LoadingIndicator(modifier = Modifier.size(20.dp))
                                    }
                                    Text(
                                        text = job.state,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (state) {
                                            "RUNNING", "ENQUEUED" -> MaterialTheme.colorScheme.primary
                                            "BLOCKED", "FAILED" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Close")
            }
            Button(
                onClick = onRetryAll,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                enabled = pendingJobs.isNotEmpty(),
            ) {
                Text("Retry all")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SyncJobsBottomSheetContentPreview() {
    SyncJobsBottomSheetContent(
        pendingJobs = listOf(
            PendingJob(operationType = SyncOperationType.CREATE, state = "Pending", bookmarkId = 1, "Bookmark 1"),
            PendingJob(operationType = SyncOperationType.UPDATE, state = "Failed", bookmarkId = 2, "Bookmark 2"),
            PendingJob(operationType = SyncOperationType.DELETE, state = "In Progress", bookmarkId = 3, "Bookmark 3")
        ),
        onDismiss = {},
        onRetryAll = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    ShioriTheme {
        TopBar(
            onSearchClick = { },
            onFilterClick = { },
            onSettingsClick = { },
            onSyncClick = { },
            selectedTagsCount = 0,
            showOnlyHiddenTag = false,
            pendingJobsCount = 0,
            pendingJobs = emptyList(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarWithPendingPreview() {
    ShioriTheme {
        TopBar(
            onSearchClick = { },
            onFilterClick = { },
            onSettingsClick = { },
            onSyncClick = { },
            selectedTagsCount = 2,
            showOnlyHiddenTag = false,
            pendingJobsCount = 3,
            pendingJobs = emptyList(),
        )
    }
}

/**
 * Right hand pane of the two pane feed: the article for whichever bookmark is open.
 *
 * Reuses ReadableContentScreen rather than a second reader. Its own BackHandler becomes "close the
 * article", which is what back should do when the list is still on screen beside it.
 */
@RequiresApi(Build.VERSION_CODES.N)
@Composable
private fun TwoPaneDetail(
    bookmarkId: Int?,
    feedViewModel: FeedViewModel,
    openUrlInBrowser: (String) -> Unit,
    onClose: () -> Unit,
) {
    if (bookmarkId == null) {
        TwoPaneEmptyDetail()
        return
    }
    val bookmark by feedViewModel.currentBookmark.collectAsState()
    LaunchedEffect(bookmarkId) { feedViewModel.loadBookmarkById(bookmarkId) }

    bookmark?.takeIf { it.id == bookmarkId }?.let {
        // Keyed so that picking a second bookmark builds a fresh reader. Without it the screen's
        // LaunchedEffect(Unit) never runs again and the pane keeps the first article.
        key(bookmarkId) {
            ReadableContentScreen(
                readableContentViewModel = koinViewModel(),
                bookmarkId = bookmarkId,
                bookmarkUrl = it.url,
                onBack = onClose,
                openUrlInBrowser = openUrlInBrowser,
                bookmarkDate = it.modified,
                bookmarkTitle = it.title,
                isRtl = it.title.isRTLText() || it.excerpt.isRTLText(),
            )
        }
    }
}
