package com.example.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.dynamicfeatures.HomeModule
import com.example.dynamicfeatures.api.DynamicModuleEntryBuilder

class HomeEntryBuilder : DynamicModuleEntryBuilder {
    override fun EntryProviderScope<NavKey>.build() {
        entry<HomeModule.Home> {
            HomeScreen()
        }
    }
}

@Composable
private fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Home Screen")
    }
}
