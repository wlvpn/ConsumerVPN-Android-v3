package com.wlvpn.consumervpn.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wlvpn.consumervpn.presentation.ui.theme.LocalColors
import com.wlvpn.consumervpn.presentation.ui.theme.elevation
import com.wlvpn.consumervpn.presentation.util.BottomNavItem
import com.wlvpn.consumervpn.presentation.util.Routes

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit,
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    if (currentRoute != null && currentRoute != Routes.Login.route) {
        NavigationBar(
            containerColor = LocalColors.current.extendedColors.backgroundNavigationBar,
            modifier = modifier,
            tonalElevation = MaterialTheme.elevation.medium
        ) {
            for (item in items) {
                val selected = item.route == currentRoute

                NavigationBarItem(
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor =
                            LocalColors.current.extendedColors.backgroundNavigationBar,
                        selectedIconColor =
                            LocalColors.current.extendedColors.controlHighlightColor,
                        unselectedIconColor =
                            LocalColors.current.extendedColors.controlNormalColor,
                        selectedTextColor =
                            LocalColors.current.extendedColors.controlHighlightColor
                    ),
                    selected = selected,
                    onClick = { onItemClick(item) },
                    label = {
                        Text(text = item.name)
                    },
                    icon = {
                        Column(horizontalAlignment = CenterHorizontally) {
                            Icon(imageVector = item.icon, contentDescription = item.name)
                        }
                    }
                )
            }
        }
    } else {
        Column {}
    }
}