package com.desarrollodroide.pagekeeper.navigation

import androidx.activity.compose.BackHandler
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.desarrollodroide.data.helpers.SHIORI_ANDROID_CLIENT_GITHUB_URL
import com.desarrollodroide.pagekeeper.navigation.NavItem
import com.desarrollodroide.pagekeeper.ui.feed.FeedRoute
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*

/**
 * Everything reachable once you are logged in.
 *
 * This was called HomeScreen and it was not a screen: it was this graph, the feed screen inlined
 * into its own route, and the feed's chrome hoisted above both. The feed now lives in FeedRoute
 * and this is only the graph, which is why it can say what it is.
 */
@Composable
fun HomeNavGraph(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    onFinish: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
    onAddManuallyClick: () -> Unit,
    shareEpubFile: (File) -> Unit,
    shareText: (String) -> Unit
) {
    val navController = rememberNavController()

    BackHandler {
        onFinish()
    }

    NavHost(
        navController = navController,
        startDestination = NavItem.HomeNavItem.route
    ) {
        composable(NavItem.HomeNavItem.route) {
            FeedRoute(
                feedViewModel = feedViewModel,
                goToLogin = goToLogin,
                openUrlInBrowser = openUrlInBrowser,
                onAddManuallyClick = onAddManuallyClick,
                shareEpubFile = shareEpubFile,
                onNavigateToSettings = { navController.navigate(NavItem.SettingsNavItem.route) },
                onOpenReadableContent = { bookmarkId ->
                    navController.navigate(
                        NavItem.ReadableContentNavItem.createRoute(bookmarkId = bookmarkId)
                    )
                },
            )
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
