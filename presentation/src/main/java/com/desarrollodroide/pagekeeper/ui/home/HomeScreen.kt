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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.get
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

    BackHandler {
        onFinish()
    }

    NavHost(
        navController = navController,
        startDestination = NavItem.HomeNavItem.route
    ) {
        composable(NavItem.HomeNavItem.route) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
                            showOnlyHiddenTag = showOnlyHiddenTag
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
    scrollBehavior: TopAppBarScrollBehavior,
    hasBookmarks: Boolean,
    selectedTagsCount: Int,
    showOnlyHiddenTag: Boolean,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    color = MaterialTheme.colorScheme.primary,
                    text = "Shiori",
                    modifier = Modifier
                        .align(Alignment.CenterStart),
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
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
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
                    Badge(
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = selectedTagsCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            IconButton(onClick = onSettingsClick ) {
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
            showOnlyHiddenTag = false
        )
    }
}

