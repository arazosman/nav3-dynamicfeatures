package com.example.dynamicfeatures.api

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Fake implementation of [DynamicFeatureInstaller] for previewing and testing progress UI.
 *
 * @param stepDelayMs Duration in milliseconds for each progress step.
 */
class FakeDynamicFeatureInstaller(
    private val stepDelayMs: Long = 800L,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : DynamicFeatureInstaller {

    override var installedModules: Set<String> by mutableStateOf(setOf("login"))
        private set

    private val statusFlows = mutableMapOf<String, MutableStateFlow<InstallStatus>>()

    private fun getOrCreateStatusFlow(moduleName: String): MutableStateFlow<InstallStatus> {
        return statusFlows.getOrPut(moduleName) {
            MutableStateFlow(
                if (isInstalled(moduleName)) InstallStatus.Installed else InstallStatus.Pending
            )
        }
    }

    override fun isInstalled(moduleName: String): Boolean =
        installedModules.contains(moduleName)

    override fun observeStatus(moduleName: String): Flow<InstallStatus> =
        getOrCreateStatusFlow(moduleName).asStateFlow()

    override fun install(moduleName: String) {
        if (isInstalled(moduleName)) return

        val statusFlow = getOrCreateStatusFlow(moduleName)
        coroutineScope.launch {
            statusFlow.value = InstallStatus.Pending
            delay(stepDelayMs.milliseconds)

            for (progress in listOf(20L, 45L, 70L, 90L, 100L)) {
                statusFlow.value = InstallStatus.Downloading(progress)
                delay(stepDelayMs.milliseconds)
            }

            statusFlow.value = InstallStatus.Installing
            delay(stepDelayMs.milliseconds)

            installedModules += moduleName
            statusFlow.value = InstallStatus.Installed
        }
    }

    override fun uninstall(moduleName: String) {
        installedModules -= moduleName
        statusFlows[moduleName]?.value = InstallStatus.Pending
    }
}