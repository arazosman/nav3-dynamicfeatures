package com.example.dynamicfeatures.api

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.errorCode
import com.google.android.play.core.ktx.moduleNames
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * [DynamicFeatureInstaller] implementation powered by Google Play Core's [SplitInstallManager].
 */
class PlayCoreDynamicFeatureInstaller(
    private val splitInstallManager: SplitInstallManager
) : DynamicFeatureInstaller {

    constructor(context: Context) : this(
        SplitInstallManagerFactory.create(context.applicationContext)
    )

    override var installedModules: Set<String> by mutableStateOf(splitInstallManager.installedModules.toSet())
        private set

    override fun isInstalled(moduleName: String): Boolean =
        installedModules.contains(moduleName)

    override fun observeStatus(moduleName: String): Flow<InstallStatus> = callbackFlow {
        if (isInstalled(moduleName)) {
            trySend(InstallStatus.Installed)
        }

        val listener = SplitInstallStateUpdatedListener { state: SplitInstallSessionState ->
            if (state.moduleNames.contains(moduleName)) {
                when (state.status) {
                    SplitInstallSessionStatus.PENDING,
                    SplitInstallSessionStatus.CANCELING -> {
                        trySend(InstallStatus.Pending)
                    }

                    SplitInstallSessionStatus.DOWNLOADING,
                    SplitInstallSessionStatus.DOWNLOADED -> {
                        val progress = if (state.totalBytesToDownload > 0) {
                            (state.bytesDownloaded * 100 / state.totalBytesToDownload)
                        } else {
                            0L
                        }
                        trySend(InstallStatus.Downloading(progress))
                    }

                    SplitInstallSessionStatus.INSTALLING -> {
                        trySend(InstallStatus.Installing)
                    }

                    SplitInstallSessionStatus.INSTALLED -> {
                        installedModules = splitInstallManager.installedModules.toSet() + moduleName
                        trySend(InstallStatus.Installed)
                    }

                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                        trySend(
                            InstallStatus.UserConfirmationRequired { launcher ->
                                splitInstallManager.startConfirmationDialogForResult(state, launcher)
                            }
                        )
                    }

                    SplitInstallSessionStatus.FAILED -> {
                        trySend(InstallStatus.Failed(state.errorCode, "Installation failed with error code: ${state.errorCode}"))
                    }

                    SplitInstallSessionStatus.CANCELED -> {
                        trySend(InstallStatus.Failed(0, "Installation was canceled."))
                    }

                    else -> Unit
                }
            }
        }

        splitInstallManager.registerListener(listener)
        awaitClose {
            splitInstallManager.unregisterListener(listener)
        }
    }

    override fun install(moduleName: String) {
        if (isInstalled(moduleName)) return
        splitInstallManager.startInstall(
            SplitInstallRequest.newBuilder().addModule(moduleName).build()
        )
    }

    override fun uninstall(moduleName: String) {
        if (!isInstalled(moduleName)) return
        splitInstallManager.deferredUninstall(listOf(moduleName))
            .addOnSuccessListener {
                installedModules = installedModules - moduleName
            }
    }
}
