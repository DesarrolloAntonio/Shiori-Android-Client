package com.desarrollodroide.pagekeeper.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.pagekeeper.ui.feed.FeedViewModel
import com.desarrollodroide.pagekeeper.ui.login.LoginScreen
import com.desarrollodroide.pagekeeper.ui.login.LoginViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.File

@RequiresApi(Build.VERSION_CODES.N)
@ExperimentalFoundationApi
@Composable
fun Navigation(
    onFinish: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
    onAddManuallyClick: () -> Unit,
    shareEpubFile: (File) -> Unit,
    shareText: (String) -> Unit
) {

    val navController = rememberNavController()
    val feedViewModel = koinViewModel<FeedViewModel>()
    val loginViewModel = koinViewModel<LoginViewModel>()

    NavHost(
        navController = navController,
        startDestination = NavItem.LoginNavItem.route
    ) {

        composable(NavItem.LoginNavItem) { backStackEntry ->
            LoginScreen(
                loginViewModel = loginViewModel,
                onSuccess = {
                    navController.navigate(NavItem.HomeNavItem.route)
                }
            )
        }
        composable(NavItem.HomeNavItem) { backStackEntry ->
            HomeNavGraph(
                feedViewModel = feedViewModel,
                goToLogin = {
                    loginViewModel.clearState()
                    feedViewModel.resetData()
                    navController.navigateToLoginAfterLogout()
                },
                onFinish = onFinish,
                openUrlInBrowser = openUrlInBrowser,
                shareEpubFile = shareEpubFile,
                shareText = shareText,
                onAddManuallyClick = onAddManuallyClick
            )
        }
    }
}

private fun NavGraphBuilder.composable(
    navItem: NavItem,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = navItem.route,
        arguments = navItem.args
    ) {
        content(it)
    }
}

private inline fun <reified T> NavBackStackEntry.findArg(key: String): T {
    val value = arguments?.get(key)
    requireNotNull(value)
    return value as T
}

/**
 * Leaves the signed-in part of the app for the login screen after a logout.
 *
 * Pops the whole graph, not just the signed-in screen: popping only that left the login the graph
 * started with underneath the new one, and Back redrew the same screen before leaving the app.
 */
internal fun NavController.navigateToLoginAfterLogout() {
    navigate(NavItem.LoginNavItem.route) {
        popUpTo(graph.id) { inclusive = true }
    }
}
