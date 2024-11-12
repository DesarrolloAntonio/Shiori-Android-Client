package com.desarrollodroide.pagekeeper.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.pagekeeper.ui.feed.FeedViewModel
import com.desarrollodroide.pagekeeper.ui.home.HomeScreen
import com.desarrollodroide.pagekeeper.ui.login.LoginScreen
import com.desarrollodroide.pagekeeper.ui.login.LoginViewModel
import org.koin.androidx.compose.get
import java.io.File

@RequiresApi(Build.VERSION_CODES.N)
@ExperimentalFoundationApi
@Composable
fun Navigation(
    onFinish: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
    shareEpubFile: (File) -> Unit,
    shareText: (String) -> Unit
) {

    val navController = rememberNavController()
    val feedViewModel = get<FeedViewModel>()
    val loginViewModel = get<LoginViewModel>()

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
            HomeScreen(
                feedViewModel = feedViewModel,
                goToLogin = {
                    loginViewModel.clearState()
                    feedViewModel.resetData()
                    navController.navigate(NavItem.LoginNavItem.route) {
                        popUpTo(NavItem.HomeNavItem.route) { inclusive = true }
                    }
                },
                onFinish = onFinish,
                openUrlInBrowser = openUrlInBrowser,
                shareEpubFile = shareEpubFile,
                shareText = shareText
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
