package com.desarrollodroide.pagekeeper.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavItem(
    internal val baseRoute: String,
    private val navArgs: List<NavArgs> = emptyList()
) {
    object SplashNavItem : NavItem("splash")
    object LoginNavItem : NavItem("login")
    object HomeNavItem : NavItem("home")
    object SettingsNavItem : NavItem("settings")
    object TermsOfUseNavItem : NavItem("termsOfUse")
    object PrivacyPolicyNavItem : NavItem("privacyPolicy")
    object ReadableContentNavItem : NavItem("readable_content/{bookmarkId}/{bookmarkUrl}/{bookmarkDate}/{bookmarkTitle}") {
        fun createRoute(bookmarkId: Int, bookmarkUrl: String, bookmarkDate: String, bookmarkTitle: String): String {
            val encodedUrl = Uri.encode(bookmarkUrl)
            val encodedDate = Uri.encode(bookmarkDate)
            val encodedTitle = Uri.encode(bookmarkTitle)
            return "readable_content/$bookmarkId/$encodedUrl/$encodedDate/$encodedTitle"
        }
    }

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