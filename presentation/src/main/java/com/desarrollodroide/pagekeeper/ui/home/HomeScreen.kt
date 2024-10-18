package com.desarrollodroide.pagekeeper.ui.home

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.get
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
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
import com.desarrollodroide.pagekeeper.ui.settings.TermsOfUseScreen
import java.io.File
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.extensions.isRTLText
import com.desarrollodroide.pagekeeper.ui.readablecontent.ReadableContentScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    onFinish: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
    shareEpubFile: (File) -> Unit,
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


    if (showBottomSheet.value) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { showBottomSheet.value = false }
        ) {
            SyncJobsBottomSheetContent(
                pendingJobs = feedViewModel.getPendingWorks().collectAsState(initial = emptyList()).value,
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
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            val pendingJobsCount by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            val pendingJobs by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    AnimatedVisibility (showTopBar) {
                        TopBar(
                            toggleCategoryVisibility = { isCategoriesVisible.value = !isCategoriesVisible.value },
                            toggleSearchBarVisibility = { isSearchBarVisible.value = !isSearchBarVisible.value },
                            onSettingsClick = { navController.navigate(NavItem.SettingsNavItem.route) },
                            scrollBehavior = scrollBehavior,
                            hasBookmarks = hasBookmarks,
                            selectedTagsCount = selectedTags.size,
                            showOnlyHiddenTag = showOnlyHiddenTag,
                            pendingJobsCount = pendingJobsCount.size,
                            onSyncButtonClick = {
                                coroutineScope.launch {
                                    showBottomSheet.value = true
                                    bottomSheetState.show()
                                }
                            },
                            pendingJobs = pendingJobs
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
                                 bookmarkUrl = bookmark.url,
                                 bookmarkDate = bookmark.modified,
                                 bookmarkTitle = bookmark.title,
                                 bookmarkIsRtl = bookmark.title.isRTLText() || bookmark.excerpt.isRTLText()
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
        composable(
            route = NavItem.ReadableContentNavItem.route,
            arguments = listOf(
                navArgument("bookmarkId") { type = NavType.IntType },
                navArgument("bookmarkUrl") { type = NavType.StringType },
                navArgument("bookmarkDate") { type = NavType.StringType },
                navArgument("bookmarkTitle") { type = NavType.StringType },
                navArgument("bookmarkIsRtl") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val bookmarkId = backStackEntry.arguments?.getInt("bookmarkId") ?: 0
            val bookmarkUrl = backStackEntry.arguments?.getString("bookmarkUrl")?.let { Uri.decode(it) } ?: ""
            val bookmarkDate = backStackEntry.arguments?.getString("bookmarkDate")?.let { Uri.decode(it) } ?: ""
            val bookmarkTitle = backStackEntry.arguments?.getString("bookmarkTitle")?.let { Uri.decode(it) } ?: ""
            val bookmarkIsRtl = backStackEntry.arguments?.getBoolean("bookmarkIsRtl")?: false

            ReadableContentScreen(
                readableContentViewModel = get(),
                bookmarkUrl = bookmarkUrl,
                bookmarkId = bookmarkId,
                bookmarkDate = bookmarkDate,
                onBack = {
                    navController.navigateUp()
                },
                openUrlInBrowser = openUrlInBrowser,
                bookmarkTitle = bookmarkTitle,
                isRtl = bookmarkIsRtl
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    toggleCategoryVisibility: () -> Unit,
    toggleSearchBarVisibility: () -> Unit,
    onSettingsClick: () -> Unit,
    onSyncButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    hasBookmarks: Boolean,
    selectedTagsCount: Int,
    showOnlyHiddenTag: Boolean,
    pendingJobsCount: Int,
    pendingJobs: List<PendingJob>
) {
    var showTooltip by remember { mutableStateOf(false) }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    color = MaterialTheme.colorScheme.primary,
                    text = "Shiori",
                    modifier = Modifier.align(Alignment.CenterStart),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp)
                )
            }
        },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.logo_pagekeeper),
                contentDescription = "Menu",
                modifier = Modifier
                    .width(45.dp)
                    .padding(8.dp)
            )
        },
        actions = {
            IconButton(onClick = { toggleSearchBarVisibility() }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { toggleCategoryVisibility() }) {
                    Icon(
                        imageVector = if (showOnlyHiddenTag) Icons.Default.VisibilityOff else Icons.Outlined.Sell,
                        contentDescription = if (showOnlyHiddenTag) "Hidden Tags" else "Filter",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                this@TopAppBar.AnimatedVisibility(
                    visible = selectedTagsCount > 0 && !showOnlyHiddenTag,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Badge(modifier = Modifier.padding(2.dp)) {
                        Text(
                            text = selectedTagsCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(
                    onClick = {
                        showTooltip = !showTooltip
                        onSyncButtonClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                this@TopAppBar.AnimatedVisibility(
                    visible = pendingJobsCount > 0,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Badge(modifier = Modifier.padding(2.dp)) {
                        Text(
                            text = pendingJobsCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

//                if (showTooltip) {
//                    Popup(
//                        onDismissRequest = { showTooltip = false },
//                        alignment = Alignment.TopEnd
//                    ) {
//                        SyncJobsTooltip(
//                            pendingJobs = pendingJobs,
//                            modifier = Modifier.padding(top = 8.dp, end = 8.dp)
//                        )
//                    }
//                }
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },

            colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background, // Sets the background color of the TopAppBar
            titleContentColor = MaterialTheme.colorScheme.primary, // Optional: Set the title color if needed
            navigationIconContentColor = MaterialTheme.colorScheme.primary, // Optional: Set the navigation icon color if needed
            actionIconContentColor = MaterialTheme.colorScheme.primary // Optional: Set the action icons color if needed
        )
    )
}

@Composable
fun SyncJobsTooltip(pendingJobs: List<PendingJob>, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = modifier.width(250.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Pending Sync Jobs",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (pendingJobs.isEmpty()) {
                Text(
                    "No pending jobs",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                pendingJobs.take(3).forEach { job ->
                    Text(
                        "${job.operationType}: ${job.state}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (job.bookmarkId != null) {
                        Text(
                            "Bookmark ID: ${job.bookmarkId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (pendingJobs.size > 3) {
                    Text(
                        "... and ${pendingJobs.size - 3} more",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

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
            .padding(bottom = 26.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
            text = "Pending Sync Jobs",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (pendingJobs.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally),
                text = "No pending jobs",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
//            // Create a header row for the table
//            Row(modifier = Modifier.padding(vertical = 4.dp)) {
//                Text(
//                    text = "ID",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//                Text(
//                    text = "Type",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//                Text(
//                    text = "State",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Actions",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            pendingJobs.forEach { job ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
//                    Text(
//                        text = job.bookmarkId?.toString() ?: "-",
//                        style = MaterialTheme.typography.bodySmall,
//                        modifier = Modifier.weight(1f)
//                    )
                    Text(
                        text = "${job.operationType.name} - ${job.bookmarkTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
//                    Text(
//                        text = job.state,
//                        style = MaterialTheme.typography.bodySmall,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    IconButton(
//                        onClick = { job.bookmarkId?.let { onRetryJob(it) } },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Retry")
//                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))

            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onRetryAll,
                modifier = Modifier.weight(1f)
            ) {
                Text("Retry All")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Close")
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
fun TopBarPreview() {
    MaterialTheme {
        TopBar(
            toggleCategoryVisibility = { },
            toggleSearchBarVisibility = { },
            onSettingsClick = { },
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            hasBookmarks = true,
            selectedTagsCount = 2,
            showOnlyHiddenTag = false,
            pendingJobsCount = 0,
            onSyncButtonClick = { },
            pendingJobs = emptyList(),
        )
    }
}

