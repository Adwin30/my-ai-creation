package com.example.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonGlow

enum class NavigationHub(val title: String) {
    CHAT("Itachi AI"),
    SETTINGS("Settings")
}

@Composable
fun ItachiBottomNav(
    selectedHub: NavigationHub,
    onSelectHub: (NavigationHub) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        modifier = modifier
            .navigationBarsPadding()
            .testTag("itachi_bottom_nav")
    ) {
        NavigationBarItem(
            selected = selectedHub == NavigationHub.CHAT,
            onClick = { onSelectHub(NavigationHub.CHAT) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Itachi Uchiha AI Chat"
                )
            },
            label = {
                Text(
                    text = "Itachi AI",
                    fontSize = 11.sp,
                    fontWeight = if (selectedHub == NavigationHub.CHAT) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonGlow,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_chat")
        )

        NavigationBarItem(
            selected = selectedHub == NavigationHub.SETTINGS,
            onClick = { onSelectHub(NavigationHub.SETTINGS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings & Custom CSS"
                )
            },
            label = {
                Text(
                    text = "Settings",
                    fontSize = 11.sp,
                    fontWeight = if (selectedHub == NavigationHub.SETTINGS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = CrimsonGlow,
                indicatorColor = CrimsonGlow,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}
