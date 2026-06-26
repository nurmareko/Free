package com.dresta0056.free.ui.nav

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dresta0056.free.model.UserProfile
import com.dresta0056.free.ui.add.AddItemScreen
import com.dresta0056.free.ui.detail.ItemDetailScreen
import com.dresta0056.free.ui.edit.EditItemScreen
import com.dresta0056.free.ui.home.HomeScreen
import com.dresta0056.free.ui.home.HomeViewModel

private const val ItemRoute = "item/{id}"
private const val AddRoute = "add"
private const val EditRoute = "edit/{id}"
private val HomeChildRoutes = setOf(ItemRoute, AddRoute, EditRoute)

@Composable
fun HomeNavHost(
    rootProfile: UserProfile,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val bottomDestinations = listOf(
        Dest.Home,
        Dest.MyPosts,
        Dest.About
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                bottomDestinations.forEach { dest ->
                    val label = stringResource(dest.labelRes)
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.route == dest.route
                        } == true || (dest == Dest.Home && currentRoute in HomeChildRoutes),
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = label
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(Dest.Home.route) {
                val context = LocalContext.current
                val vm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(context.applicationContext)
                )
                val state by vm.uiState.collectAsState()

                HomeScreen(
                    state = state,
                    onItemClick = { itemId ->
                        navController.navigate("item/${Uri.encode(itemId)}")
                    },
                    onAddClick = {
                        navController.navigate(AddRoute)
                    },
                    onRefresh = vm::refresh,
                    onErrorShown = vm::consumeError
                )
            }
            composable(Dest.MyPosts.route) {
                com.dresta0056.free.ui.myposts.MyPostsScreen(
                    onItemClick = { id ->
                        navController.navigate("item/${Uri.encode(id)}")
                    }
                )
            }
            composable(Dest.About.route) {
                com.dresta0056.free.ui.about.AboutScreen(
                    profile = rootProfile,
                    onLogout = onLogout
                )
            }
            composable(
                route = ItemRoute,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val itemId = entry.arguments?.getString("id").orEmpty()
                ItemDetailScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("edit/${Uri.encode(id)}") }
                )
            }
            composable(AddRoute) {
                AddItemScreen(
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() }
                )
            }
            composable(
                route = EditRoute,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.StringType
                    }
                )
            ) { entry ->
                val itemId = entry.arguments?.getString("id").orEmpty()
                EditItemScreen(
                    itemId = itemId,
                    onDone = { navController.popBackStack() },
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
