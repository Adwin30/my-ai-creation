package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ItachiBottomNav
import com.example.ui.components.ItachiTopBar
import com.example.ui.components.NavigationHub
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ItachiTheme

@Composable
fun ItachiApp(
    viewModel: ItachiViewModel = viewModel()
) {
    var currentHub by rememberSaveable { mutableStateOf(NavigationHub.CHAT) }

    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val customPrimary by viewModel.customPrimaryColor.collectAsStateWithLifecycle()
    val customBg by viewModel.customBgColor.collectAsStateWithLifecycle()
    val isSyncActive by viewModel.isSyncActive.collectAsStateWithLifecycle()

    ItachiTheme(
        themeName = currentTheme,
        isDarkMode = isDarkMode,
        customPrimaryColor = customPrimary,
        customBgColor = customBg
    ) {
        Scaffold(
            topBar = {
                ItachiTopBar(
                    currentScreenTitle = when (currentHub) {
                        NavigationHub.CHAT -> "ITACHI UCHIHA AI"
                        NavigationHub.SETTINGS -> "TSUKUYOMI SETTINGS"
                    },
                    isSyncActive = isSyncActive
                )
            },
            bottomBar = {
                ItachiBottomNav(
                    selectedHub = currentHub,
                    onSelectHub = { currentHub = it }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentHub) {
                    NavigationHub.CHAT -> ChatScreen(
                        viewModel = viewModel
                    )
                    NavigationHub.SETTINGS -> SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
