package com.example.dynamicfeatures.api

import kotlinx.coroutines.flow.Flow

/**
 * Interface that manages dynamic feature module installation, uninstallation,
 * and status observation.
 */
interface DynamicFeatureInstaller {
    val installedModules: Set<String>
    fun isInstalled(moduleName: String): Boolean
    fun observeStatus(moduleName: String): Flow<InstallStatus>
    fun install(moduleName: String)
    fun uninstall(moduleName: String)
}
