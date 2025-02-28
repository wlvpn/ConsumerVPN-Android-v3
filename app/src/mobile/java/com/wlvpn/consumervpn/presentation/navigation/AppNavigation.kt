package com.wlvpn.consumervpn.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wlvpn.consumervpn.presentation.home.HomeScreen
import com.wlvpn.consumervpn.presentation.home.HomeViewModel
import com.wlvpn.consumervpn.presentation.locations.LocationsScreen
import com.wlvpn.consumervpn.presentation.locations.LocationsViewModel
import com.wlvpn.consumervpn.presentation.login.LoginViewModel
import com.wlvpn.consumervpn.presentation.login.ui.LoginScreen
import com.wlvpn.consumervpn.presentation.settings.SettingsScreen
import com.wlvpn.consumervpn.presentation.settings.SettingsViewModel
import com.wlvpn.consumervpn.presentation.util.Routes

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String,
    closeApp: () -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
) {

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Routes.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLogin = {
                    navController.navigate(route = Routes.Home.route)
                },
                onBackPressed = { closeApp() },
                onForgotPassword = { onForgotPassword() },
                onSignUp = { onSignUp() }
            )
        }

        composable(route = Routes.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel,
                onBackPressed = { closeApp() }
            )
        }

        composable(route = Routes.Locations.route) {
            val viewModel: LocationsViewModel = hiltViewModel()

            LocationsScreen(
                viewModel = viewModel,
                onConnect = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Home.route) {
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = Routes.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() },
                onNavigateBack = { navController.navigate(Routes.Home.route) }
            )
        }
    }
}