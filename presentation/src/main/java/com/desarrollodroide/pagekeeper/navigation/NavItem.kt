package com.desarrollodroide.pagekeeper.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavItem(
    internal val baseRoute: String,
    private val navArgs: List<NavArgs> = emptyList()
) {
    object SplashNavItem : NavItem("splash")
    object LoginNavItem : NavItem("login")
    object HomeNavItem : NavItem("home")
    object RegisterNavItem : NavItem("register")
    object SettingsNavItem : NavItem("settings")

    object FeedNavItem : NavItem("feed")

        val route = run {
        val argValues = navArgs.map { "{${it.key}}" }
        listOf(baseRoute)
            .plus(argValues)
            .joinToString("/")
    }

    val args = navArgs.map {
        navArgument(it.key) { type = it.navType }
    }
}

enum class NavArgs(val key: String, val navType: NavType<*>)