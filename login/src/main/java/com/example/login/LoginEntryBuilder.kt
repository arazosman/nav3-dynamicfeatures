package com.example.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.dynamicfeatures.LoginModule
import com.example.dynamicfeatures.api.DynamicModuleEntryBuilder

class LoginEntryBuilder : DynamicModuleEntryBuilder {
    override fun EntryProviderScope<NavKey>.build() {
        entry<LoginModule.Login> {
            LoginScreen()
        }
    }
}

@Composable
private fun LoginScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Login Screen")
    }
}
