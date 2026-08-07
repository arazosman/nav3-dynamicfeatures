package com.example.dynamicfeatures.api

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

/**
 * Represents the observable lifecycle status of a dynamic feature module installation.
 */
sealed interface InstallStatus {
    data object Pending : InstallStatus
    data class Downloading(val progress: Long) : InstallStatus
    data object Installing : InstallStatus
    data object Installed : InstallStatus
    data class UserConfirmationRequired(
        val onConfirm: (ActivityResultLauncher<IntentSenderRequest>) -> Unit
    ) : InstallStatus
    data class Failed(val errorCode: Int, val message: String? = null) : InstallStatus
}
