package com.example.dynamicfeatures

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dynamicfeatures.api.DynamicFeatureInstaller
import com.example.dynamicfeatures.api.FakeDynamicFeatureInstaller
import com.example.dynamicfeatures.api.InstallStatus
import com.example.dynamicfeatures.api.dynamicEntry
import com.example.dynamicfeatures.api.dynamicFeatureDecorator
import com.example.dynamicfeatures.ui.theme.DynamicFeaturesTheme
import com.google.android.play.core.splitcompat.SplitCompat
import kotlinx.serialization.Serializable

@Serializable
private data object MainScreenKey : NavKey

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DynamicFeaturesTheme {
                val backStack: NavBackStack<NavKey> = rememberNavBackStack(MainScreenKey)
                val installer: DynamicFeatureInstaller = retain { FakeDynamicFeatureInstaller(stepDelayMs = 800L) }

                // Observe Home status for async download UI
                val homeStatus by installer.observeStatus(HomeModule.module.moduleName)
                    .collectAsState(
                        initial = if (installer.isInstalled(HomeModule.module.moduleName)) {
                            InstallStatus.Installed
                        } else {
                            InstallStatus.Pending
                        }
                    )

                NavDisplay(
                    backStack = backStack,
                    onBack = backStack::removeLastOrNull,
                    entryDecorators = listOf(
                        dynamicFeatureDecorator(
                            installer = installer,
                            loadingContent = { status: InstallStatus, key: NavKey ->
                                when (key) {
                                    is LoginModule.Login -> {
                                        DynamicFeatureProgressScreen(
                                            moduleName = "Login Module",
                                            status = status,
                                            onCancel = { backStack.removeLastOrNull() }
                                        )
                                    }

                                    else -> Unit
                                }
                            }
                        )
                    ),
                    entryProvider = entryProvider {
                        entry<MainScreenKey> {
                            MainAppScreen(
                                homeStatus = homeStatus,
                                onNavigateToLogin = {
                                    backStack.add(LoginModule.Login)
                                },
                                onInstallHomeAsync = {
                                    installer.install(HomeModule.module.moduleName)
                                },
                                onNavigateToHome = {
                                    backStack.add(HomeModule.Home)
                                },
                                onUninstallLogin = {
                                    backStack.removeAll { it is LoginModule.Login }
                                    installer.uninstall(LoginModule.module.moduleName)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Requested deferred uninstallation of login module",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onUninstallHome = {
                                    backStack.removeAll { it is HomeModule.Home }
                                    installer.uninstall(HomeModule.module.moduleName)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Requested deferred uninstallation of home module",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }

                        dynamicEntry<LoginModule.Login>()
                        dynamicEntry<HomeModule.Home>()
                    }
                )
            }
        }
    }
}

@Composable
private fun MainAppScreen(
    homeStatus: InstallStatus,
    onNavigateToLogin: () -> Unit,
    onInstallHomeAsync: () -> Unit,
    onNavigateToHome: () -> Unit,
    onUninstallLogin: () -> Unit,
    onUninstallHome: () -> Unit,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dynamic Feature Modules in Nav 3",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Login Module (Sync On-Demand)")
            }

            when (homeStatus) {
                is InstallStatus.Installed -> {
                    ElevatedButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Navigate to Home (Installed)")
                    }
                }

                is InstallStatus.Downloading -> {
                    ElevatedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Downloading Home: ${homeStatus.progress}%")
                    }
                }

                is InstallStatus.Installing -> {
                    ElevatedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Installing Home...")
                    }
                }

                is InstallStatus.Pending -> {
                    ElevatedButton(
                        onClick = onInstallHomeAsync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Install Home Module (Async in Background)")
                    }
                }

                is InstallStatus.UserConfirmationRequired -> {
                    ElevatedButton(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home: User Confirmation Required")
                    }
                }

                is InstallStatus.Failed -> {
                    ElevatedButton(
                        onClick = onInstallHomeAsync,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home Download Failed. Retry?")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onUninstallLogin,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Uninstall Login Module (Deferred)")
            }

            OutlinedButton(
                onClick = onUninstallHome,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Uninstall Home Module (Deferred)")
            }
        }
    }
}

@Composable
private fun DynamicFeatureProgressScreen(
    moduleName: String,
    status: InstallStatus,
    onCancel: () -> Unit,
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
                    text = moduleName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                when (status) {
                    is InstallStatus.Pending -> {
                        CircularProgressIndicator()
                        Text("Initiating installation...")
                    }

                    is InstallStatus.Downloading -> {
                        LinearProgressIndicator(
                            progress = { status.progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text("Downloading: ${status.progress}%")
                    }

                    is InstallStatus.Installing, is InstallStatus.Installed -> {
                        CircularProgressIndicator()
                        Text("Installing feature module...")
                    }

                    is InstallStatus.UserConfirmationRequired -> {
                        Text("User confirmation required to continue.")
                    }

                    is InstallStatus.Failed -> {
                        Text(
                            text = "Failed to install: ${status.message ?: "Error ${status.errorCode}"}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                OutlinedButton(onClick = onCancel) {
                    Text("Cancel / Back")
                }
            }
        }
    }
}
