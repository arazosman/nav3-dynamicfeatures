package com.example.dynamicfeatures

import androidx.navigation3.runtime.NavKey
import com.example.dynamicfeatures.api.DynamicModule
import kotlinx.serialization.Serializable

object LoginModule {
    val module = DynamicModule(
        entryBuilderClassName = "com.example.login.LoginEntryBuilder",
        moduleName = "login",
    )

    @Serializable
    data object Login : NavKey
}

object HomeModule {
    val module = DynamicModule(
        entryBuilderClassName = "com.example.home.HomeEntryBuilder",
        moduleName = "home",
    )

    @Serializable
    data object Home : NavKey
}
