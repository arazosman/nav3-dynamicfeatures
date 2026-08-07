package com.example.dynamicfeatures

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.errorCode
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

/**
 * Sample App UI composable for dynamic feature installation progress.
 */
@Composable
fun DynamicFeatureProgressScreen(
    moduleName: String,
    sessionState: SplitInstallSessionState?,
    onCancel: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Delivering Feature",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Module: $moduleName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (sessionState?.status) {
                    null,
                    SplitInstallSessionStatus.PENDING -> {
                        CircularProgressIndicator()
                        Text("Waiting for network...")
                    }

                    SplitInstallSessionStatus.DOWNLOADING -> {
                        val total = sessionState.totalBytesToDownload
                        val downloaded = sessionState.bytesDownloaded
                        val progress = if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
                        val percent = (progress * 100).toInt()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("$percent% downloaded")
                    }

                    SplitInstallSessionStatus.INSTALLING,
                    SplitInstallSessionStatus.DOWNLOADED -> {
                        CircularProgressIndicator()
                        Text("Splits installing onto device...")
                    }

                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                        Text(
                            "⚠️ User confirmation required by Google Play.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    SplitInstallSessionStatus.FAILED -> {
                        Text(
                            "⚠️ Installation failed (${sessionState.errorCode}): Error during feature delivery",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    SplitInstallSessionStatus.CANCELED -> {
                        Text("Installation was canceled.", color = MaterialTheme.colorScheme.error)
                    }

                    SplitInstallSessionStatus.INSTALLED -> {
                        Text("Ready!")
                    }

                    else -> {
                        CircularProgressIndicator()
                    }
                }

                if (onCancel != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
