package com.desarrollodroide.pagekeeper.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.desarrollodroide.data.helpers.SHIORI_ANDROID_CLIENT_GITHUB_URL
import com.desarrollodroide.pagekeeper.navigation.NavItem
import com.desarrollodroide.pagekeeper.ui.feed.AddTagsToSelectionDialog
import com.desarrollodroide.pagekeeper.ui.feed.FeedScreen
import com.desarrollodroide.pagekeeper.ui.feed.SyncJobsBottomSheetContent
import com.desarrollodroide.pagekeeper.ui.feed.TopBar
import com.desarrollodroide.pagekeeper.ui.feed.SelectionTopBar
import com.desarrollodroide.pagekeeper.ui.feed.TwoPaneDetail
import com.desarrollodroide.pagekeeper.ui.feed.FeedViewModel
import com.desarrollodroide.pagekeeper.ui.settings.PrivacyPolicyScreen
import com.desarrollodroide.pagekeeper.ui.settings.SettingsScreen
import com.desarrollodroide.pagekeeper.ui.tags.TagsScreen
import com.desarrollodroide.pagekeeper.ui.settings.TermsOfUseScreen
import java.io.File
import com.desarrollodroide.pagekeeper.extensions.isRTLText
import com.desarrollodroide.pagekeeper.ui.readablecontent.ReadableContentScreen
import com.desarrollodroide.pagekeeper.ui.settings.crash.CrashLogScreen
import com.desarrollodroide.pagekeeper.ui.settings.logcat.NetworkLogScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.CompositionLocalProvider
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorScreen
import com.desarrollodroide.pagekeeper.ui.bookmarkeditor.BookmarkEditorType
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.desarrollodroide.pagekeeper.ui.components.ListPaneWidth
import com.desarrollodroide.pagekeeper.ui.components.LocalFeedInListPane
import com.desarrollodroide.pagekeeper.ui.components.shouldUseTwoPanes
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.remember
import com.desarrollodroide.pagekeeper.ui.components.ConfirmDialog
import com.desarrollodroide.pagekeeper.ui.components.UpdateCacheDialog

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
    val (showTopBar, setShowTopBar) = remember { mutableStateOf(true) }
    val searchQuery by feedViewModel.searchQuery.collectAsState()
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
            val selectedBookmarks by feedViewModel.selectedBookmarks.collectAsStateWithLifecycle()
            val showDeleteSelectedDialog = remember { mutableStateOf(false) }
            val showUpdateSelectedDialog = remember { mutableStateOf(false) }
            val showAddTagsDialog = remember { mutableStateOf(false) }

            if (showAddTagsDialog.value) {
                AddTagsToSelectionDialog(
                    selectedCount = selectedBookmarks.size,
                    onDismiss = { showAddTagsDialog.value = false },
                    onConfirm = { names ->
                        feedViewModel.addTagsToSelected(names)
                        showAddTagsDialog.value = false
                    },
                )
            }

            if (showDeleteSelectedDialog.value) {
                ConfirmDialog(
                    title = "Delete bookmarks",
                    content = "Delete ${selectedBookmarks.size} selected bookmarks? This action is irreversible.",
                    confirmButton = "Delete",
                    dismissButton = "Cancel",
                    onConfirm = {
                        feedViewModel.deleteSelected()
                        showDeleteSelectedDialog.value = false
                    },
                    openDialog = showDeleteSelectedDialog,
                )
            }
            UpdateCacheDialog(
                isLoading = false,
                showDialog = showUpdateSelectedDialog,
                onConfirm = { keepOldTitle, updateArchive, updateEbook ->
                    feedViewModel.updateCacheForSelected(keepOldTitle, updateArchive, updateEbook)
                    showUpdateSelectedDialog.value = false
                },
            )

            val pendingJobsCount by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            val pendingJobs by feedViewModel.getPendingWorks().collectAsState(initial = emptyList())
            val useTwoPaneLayout by feedViewModel.useTwoPaneLayout.collectAsStateWithLifecycle()
            // Which bookmark the detail pane is showing. Only the id is kept: Bookmark is not
            // parcelable, and the feed view model can resolve it again after a rotation.
            var openBookmarkId by rememberSaveable { mutableStateOf<Int?>(null) }

            // Measured rather than taken from a window size class, so the same decision can be
            // driven from a test by handing the composable a width. It sits outside the scaffold
            // because the fab slot needs the answer too: in two panes the add button belongs to
            // the list, not to the window, and the scaffold would otherwise put it over the
            // article.
            BoxWithConstraints {
            val twoPanes = shouldUseTwoPanes(useTwoPaneLayout, maxWidth)
            // Editing is a mode, not a view: it takes the window rather than a pane. Inside the
            // feed it inherited whatever width the feed had, which in two panes is the 340dp list
            // column with the other two thirds showing "Nothing open" beside it. Up here it also
            // gets the editor's own two column layout on a short window, which a 620dp pane is too
            // narrow to trigger and too short to do without.
            val editingBookmark = feedViewModel.bookmarkSelected.value
            val editorOpen = editingBookmark != null && feedViewModel.showBookmarkEditorScreen.value
            val addBookmarkFab: @Composable () -> Unit = {
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
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    // Batch edit takes the app bar over, the way a contextual action bar does, so
                    // the actions apply to the selection rather than to the whole feed.
                    if (selectedBookmarks.isNotEmpty()) {
                        SelectionTopBar(
                            selectedCount = selectedBookmarks.size,
                            onClose = { feedViewModel.clearSelection() },
                            onDelete = { showDeleteSelectedDialog.value = true },
                            onUpdateCache = { showUpdateSelectedDialog.value = true },
                            onAddTags = { showAddTagsDialog.value = true },
                        )
                    } else
                    AnimatedVisibility (showTopBar) {
                        TopBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = feedViewModel::updateSearchQuery,
                            onClearSearch = feedViewModel::clearSearch,
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
                floatingActionButton = { if (!twoPanes) addBookmarkFab() }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    val feed: @Composable (Modifier) -> Unit = { _ ->
                        FeedScreen(
                            feedViewModel = feedViewModel,
                            isCategoriesVisible = isCategoriesVisible,
                            goToLogin = goToLogin,
                            openUrlInBrowser = openUrlInBrowser,
                            shareEpubFile = shareEpubFile,
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

                    when {
                        twoPanes -> {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.width(ListPaneWidth)) {
                                    CompositionLocalProvider(LocalFeedInListPane provides true) {
                                        feed(Modifier)
                                    }
                                    // Both floating buttons belong to the feed, so in two panes they
                                    // move into the list with it. Left in the scaffold, the add button
                                    // sat at the bottom of the window, which is on top of the article.
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(16.dp)
                                    ) {
                                        addBookmarkFab()
                                    }
                                }
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
                        }

                        else -> {
                            // The pane can only have been opened on a wider window. Drop it, or
                            // the selection would come back on the next rotation into landscape.
                            LaunchedEffect(twoPanes) { openBookmarkId = null }
                            feed(Modifier)
                        }
                    }
                }
            }
            // Over the scaffold, not inside its content. Inside, hiding the feed's app bar
            // animated the content padding away underneath the editor: it slid up to the status
            // bar over the course of the animation and then dropped back when its own app bar
            // laid out. Nothing behind it moves now, because nothing behind it is relaid out.
            // Opening it is a transition, not a state change appearing out of nowhere. The + is
            // an activity, so the system animates it in; this is a composable in the same
            // window and gets nothing for free. Reading the bookmark rather than the flag is
            // deliberate: bookmarkSelected outlives the flag, so the exit still has something
            // to draw on the way out.
            AnimatedVisibility(
                visible = editorOpen,
                enter = fadeIn(tween(220)) + slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 12 },
                exit = fadeOut(tween(180)) + slideOutVertically(tween(180, easing = FastOutSlowInEasing)) { it / 12 },
            ) {
                editingBookmark ?: return@AnimatedVisibility
                // Swallows taps so nothing behind it reacts, the same as when it lived in
                // the feed.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { } }
                ) {
                    BookmarkEditorScreen(
                        // Already inside the home scaffold's padded content.
                        // systemBars, not WindowInsets(0): the editor is no longer inside the
                        // scaffold's padded content, so it has to inset itself. Its app bar
                        // takes the top from this same value, and at zero it drew "Edit"
                        // across the clock.
                        windowInsets = WindowInsets.systemBars,
                        pageTitle = "Edit",
                        bookmarkEditorType = BookmarkEditorType.EDIT,
                        bookmark = editingBookmark,
                        onBack = { feedViewModel.showBookmarkEditorScreen.value = false },
                        // No sync here. editBookmark writes the server's own response into
                        // Room before returning, and the feed is a Room pager, so the card
                        // repaints on its own. Refreshing meant walking every page of the
                        // server to be told what the app had just written: 334 requests to
                        // save an edit, on a library of ten thousand.
                        updateBookmark = { feedViewModel.showBookmarkEditorScreen.value = false },
                    )
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
