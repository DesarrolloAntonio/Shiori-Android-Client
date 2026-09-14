package com.desarrollodroide.pagekeeper.navigation

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * After logout the login screen is the only thing on the back stack, so one Back leaves the app.
 *
 * Logout popped the signed-in screen and pushed a new login on top of the login the graph had
 * started with: Back redrew the same login screen and only the second press left. Seen on a device.
 */
@RunWith(AndroidJUnit4::class)
class LogoutBackStackTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var navController: NavHostController

    private fun signedIn() {
        rule.setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination = NavItem.LoginNavItem.route) {
                composable(NavItem.LoginNavItem.route) {}
                composable(NavItem.HomeNavItem.route) {}
            }
        }
        rule.runOnIdle { navController.navigate(NavItem.HomeNavItem.route) }
    }

    @Test
    fun afterLogoutLoginIsTheOnlyScreenLeft() {
        signedIn()

        rule.runOnIdle { navController.navigateToLoginAfterLogout() }

        rule.runOnIdle {
            assertEquals(NavItem.LoginNavItem.route, navController.currentDestination?.route)
            assertNull("nothing may be left under the login screen", navController.previousBackStackEntry)
        }
    }

    /** The pair (R7): while signed in, the signed-in screen does sit on top of the start. */
    @Test
    fun whileSignedInThereIsAScreenUnderneath() {
        signedIn()

        rule.runOnIdle {
            assertEquals(NavItem.HomeNavItem.route, navController.currentDestination?.route)
            assertEquals(NavItem.LoginNavItem.route, navController.previousBackStackEntry?.destination?.route)
        }
    }
}
