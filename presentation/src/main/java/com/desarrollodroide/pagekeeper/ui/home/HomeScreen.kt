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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.get
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
            // exitUntilCollapsed lets the two-row flexible bar collapse to a single row as the feed scrolls.
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            val pendingJobsCount by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            val pendingJobs by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    AnimatedVisibility (showTopBar) {
                        TopBar(
                            onSettingsClick = { navController.navigate(NavItem.SettingsNavItem.route) },
                            scrollBehavior = scrollBehavior,
                            selectedTagsCount = selectedTags.size,
                            showOnlyHiddenTag = showOnlyHiddenTag,
                            pendingJobsCount = pendingJobsCount.size,
                        )
                    }
                },
                bottomBar = {
                    AnimatedVisibility(showTopBar) {
                        FeedBottomBar(
                            onSearchClick = { isSearchBarVisible.value = !isSearchBarVisible.value },
                            onFilterClick = { isCategoriesVisible.value = !isCategoriesVisible.value },
                            onSyncClick = {
                                coroutineScope.launch {
                                    showBottomSheet.value = true
                                    bottomSheetState.show()
                                }
                            },
                            onAddClick = onAddManuallyClick,
                            selectedTagsCount = selectedTags.size,
                            showOnlyHiddenTag = showOnlyHiddenTag,
                            pendingJobsCount = pendingJobsCount.size,
                            pendingJobs = pendingJobs,
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                ) {
                    FeedScreen(
                        feedViewModel = feedViewModel,
                        isCategoriesVisible = isCategoriesVisible,
                        goToLogin = goToLogin,
                        openUrlInBrowser = openUrlInBrowser,
                        shareEpubFile = shareEpubFile,
                        isSearchBarVisible = isSearchBarVisible,
                        setShowTopBar = setShowTopBar,
                        goToReadableContent = { bookmark->
                             navController.navigate(NavItem.ReadableContentNavItem.createRoute(
                                 bookmarkId = bookmark.id,
                             ))
                        },
                    )
                }
            }
        }
        composable(NavItem.SettingsNavItem.route) {
            SettingsScreen(
                settingsViewModel = get(),
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
                tagsViewModel = get(),
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
                    readableContentViewModel = get(),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBar(
    onSettingsClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    selectedTagsCount: Int,
    showOnlyHiddenTag: Boolean,
    pendingJobsCount: Int,
) {
    val subtitle = when {
        pendingJobsCount > 0 -> "$pendingJobsCount pending to sync"
        showOnlyHiddenTag -> "Hidden tags only"
        selectedTagsCount > 0 -> "$selectedTagsCount tag filter" + if (selectedTagsCount > 1) "s" else ""
        else -> null
    }

    // The bar carries identity and state, not a row of controls. Everything actionable moved to
    // the bottom bar, where it is reachable one handed: five icons up here had no hierarchy and
    // put the primary action, adding a bookmark, in the hardest corner to reach on a tall phone.
    MediumFlexibleTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = "Shiori",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        subtitle = {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.logo_pagekeeper),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(36.dp)
            )
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

/**
 * The feed's actions, in a bottom bar with the primary one as a docked fab.
 *
 * Search, filtering and sync are the things a reader reaches for repeatedly, so they sit within
 * thumb reach rather than in the top corner. Adding a bookmark is the primary action and gets the
 * fab, which is what M3 asks for and what the top bar's small plus icon was not.
 */
@Composable
private fun FeedBottomBar(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onSyncClick: () -> Unit,
    onAddClick: () -> Unit,
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

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
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
            BadgedIconButton(
                onClick = onSyncClick,
                badgeCount = pendingJobsCount,
                description = "Sync",
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add bookmark")
            }
        },
    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    ShioriTheme {
        TopBar(
            onSettingsClick = { },
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            selectedTagsCount = 2,
            showOnlyHiddenTag = false,
            pendingJobsCount = 0,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedBottomBarPreview() {
    ShioriTheme {
        FeedBottomBar(
            onSearchClick = { },
            onFilterClick = { },
            onSyncClick = { },
            onAddClick = { },
            selectedTagsCount = 2,
            showOnlyHiddenTag = false,
            pendingJobsCount = 3,
            pendingJobs = emptyList(),
        )
    }
}
