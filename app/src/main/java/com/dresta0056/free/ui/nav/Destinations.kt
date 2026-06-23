package com.dresta0056.free.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.ui.graphics.vector.ImageVector
import com.dresta0056.free.R

sealed class Dest(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector
) {
    data object Home : Dest("home", R.string.nav_home, Icons.Filled.Home)
    data object MyPosts : Dest("my_posts", R.string.nav_my_posts, Icons.Filled.Inventory2)
    data object About : Dest("about", R.string.nav_about, Icons.Filled.Info)
}
