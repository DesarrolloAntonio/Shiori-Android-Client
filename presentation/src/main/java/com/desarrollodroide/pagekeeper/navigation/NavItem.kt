package com.desarrollodroide.pagekeeper.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavItem(
    internal val baseRoute: String,
    private val navArgs: List<NavArgs> = emptyList()
) {
    data object LoginNavItem : NavItem("login")
    data object HomeNavItem : NavItem("home")
    data object SettingsNavItem : NavItem("settings")
    data object TermsOfUseNavItem : NavItem("termsOfUse")
    data object PrivacyPolicyNavItem : NavItem("privacyPolicy")
    data object ReadableContentNavItem : NavItem("readable_content/{bookmarkId}") {
        fun createRoute(bookmarkId: Int): String {
            return "readable_content/$bookmarkId"
        }
    }
    data object NetworkLoggerNavItem : NavItem("networkLogger")
    data object LastCrashNavItem: NavItem("lastCrash")

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