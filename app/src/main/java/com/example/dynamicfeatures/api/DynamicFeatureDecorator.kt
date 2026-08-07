package com.example.dynamicfeatures.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.get

/**
 * A Navigation 3 [NavEntryDecorator] that intercepts dynamic destinations via [DynamicModuleMetadataKey].
 *
 * - When the module is **installed**: lazily resolves the dynamic module's entry builder via reflection
 *   and renders the dynamic screen.
 * - When the module is **not installed**: automatically initiates installation and renders [loadingContent].
 */
inline fun <reified T : Any> dynamicFeatureDecorator(
    installer: DynamicFeatureInstaller,
    noinline loadingContent: @Composable (status: InstallStatus, key: T) -> Unit = { _, _ -> }
): NavEntryDecorator<T> = NavEntryDecorator { entry: NavEntry<T> ->
    val dynamicModule = entry.metadata[DynamicModuleMetadataKey]

    if (dynamicModule != null) {
        val status by installer.observeStatus(dynamicModule.moduleName)
            .collectAsState(
                initial = if (installer.isInstalled(dynamicModule.moduleName)) {
                    InstallStatus.Installed
                } else {
                    InstallStatus.Pending
                }
            )

        if (status is InstallStatus.Installed) {
            val dynamicEntry = remember(dynamicModule.entryBuilderClassName, entry.contentKey) {
                entryProvider {
                    dynamicModule.build(this)
                }.invoke(entry.contentKey as NavKey)
            }
            dynamicEntry.Content()
        } else {
            LaunchedEffect(dynamicModule.moduleName) {
                installer.install(dynamicModule.moduleName)
            }
            val key = entry.contentKey as? T
            if (key != null) {
                loadingContent(status, key)
            } else {
                entry.Content()
            }
        }
    } else {
        entry.Content()
    }
}
