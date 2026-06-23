package com.dresta0056.free.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Dest(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : Dest("home", "Home", Icons.Filled.Home)
    data object MyPosts : Dest("my_posts", "My Posts", Icons.Filled.Inventory2)
    data object About : Dest("about", "About", Icons.Filled.Info)
}
