package com.example.helloworld.ui.component


import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.helloworld.R
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("routine", "루틴", R.drawable.ic_routine_default, R.drawable.ic_routine_selected),
        BottomNavItem("camera", "인증", R.drawable.ic_camera_default, R.drawable.ic_camera_selected),
        BottomNavItem("diary", "기록", R.drawable.ic_diary_default, R.drawable.ic_diary_selected)
    )

    NavigationBar(containerColor = Color.White) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            val selected = currentRoute == item.route
            val iconRes = if (selected) item.selectedIconRes else item.defaultIconRes
            val labelColor = if (selected) Color(0xFF2979FF) else Color(0xFF888888)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(30.dp)
                    )
                },
                label = {
                    Text(item.label, color = labelColor)
                },
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val defaultIconRes: Int,
    val selectedIconRes: Int
)
