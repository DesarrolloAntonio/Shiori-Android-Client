package com.shiori.androidclient.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shiori.androidclient.ui.login.LoginScreen
import com.shiori.androidclient.ui.login.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@ExperimentalFoundationApi
@Composable
fun Navigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavItem.LoginNavItem.route
    ) {
        composable(NavItem.SplashNavItem) {

        }
        composable(NavItem.LoginNavItem) { backStackEntry ->
            LoginScreen(
                loginViewModel = koinViewModel<LoginViewModel>(),
                onRegister = {}
//                onRegister = {
//                    navController.navigate(NavItem.RegisterNavItem.route)
//                }, loginViewModel = getComposeViewModel()
            )
        }
        composable(NavItem.RegisterNavItem) { backStackEntry ->
//            RegisterScreen(getComposeViewModel())
        }
        composable(NavItem.HomeNavItem) { backStackEntry ->

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
