package com.shiori.androidclient.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shiori.androidclient.ui.feed.FeedScreen
import com.shiori.androidclient.ui.feed.FeedViewModel
import com.shiori.androidclient.ui.home.HomeScreen
import com.shiori.androidclient.ui.login.LoginScreen
import com.shiori.androidclient.ui.login.LoginViewModel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@ExperimentalFoundationApi
@Composable
fun Navigation(
    onFinish: () -> Unit,
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
                onRegister = {},
                onSuccess = {
                    navController.navigate(NavItem.HomeNavItem.route)
                }
            )
        }
        composable(NavItem.HomeNavItem) { backStackEntry ->
            HomeScreen(
                feedViewModel = feedViewModel,
                goToLogin = {
//                    feedViewModel.clearError()
                    navController.navigate(NavItem.LoginNavItem.route) {
                        popUpTo(NavItem.HomeNavItem.route) { inclusive = true }
                    }
                },
                onFinish = onFinish
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
