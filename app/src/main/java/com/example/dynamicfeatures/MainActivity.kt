package com.example.dynamicfeatures

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.dynamicModule
import androidx.navigation3.rememberDynamicFeatureDecorator
import androidx.navigation3.rememberSplitInstallManager
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dynamicfeatures.api.LocalNavBackStack
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
                val splitInstallManager = rememberSplitInstallManager()
                val dynamicDecorator = rememberDynamicFeatureDecorator(
                    splitInstallManager = splitInstallManager,
                    defaultLoadingContent = { sessionState ->
                        DynamicFeatureProgressScreen(
                            moduleName = "Feature Delivery (Default Decorator UI)",
                            sessionState = sessionState,
                            onCancel = { backStack.removeLastOrNull() }
                        )
                    }
                )

                CompositionLocalProvider(LocalNavBackStack provides backStack) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = backStack::removeLastOrNull,
                        entryDecorators = listOf(dynamicDecorator),
                        entryProvider = entryProvider {
                            entry<MainScreenKey> {
                                MainAppScreen(
                                    onStartOnboarding = {
                                        backStack.add(OnboardingKey)
                                    },
                                    onNavigateToPremium = {
                                        backStack.add(PremiumKey)
                                    },
                                    onUninstallOnboarding = {
                                        splitInstallManager.deferredUninstall(listOf(OnboardingModule.moduleName))
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Requested deferred uninstallation of onboarding module",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onUninstallPremium = {
                                        splitInstallManager.deferredUninstall(listOf(PremiumModule.moduleName))
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Requested deferred uninstallation of premium module",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }

                            dynamicModule(OnboardingModule)
                            dynamicModule(PremiumModule)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainAppScreen(
    onStartOnboarding: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onUninstallOnboarding: () -> Unit,
    onUninstallPremium: () -> Unit,
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pure navigation-driven delivery: Navigating to a dynamic key automatically downloads & transitions to it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = onStartOnboarding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Onboarding (Custom Module Loading UI)")
            }

            ElevatedButton(
                onClick = onNavigateToPremium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Premium (Default Decorator Loading UI)")
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onUninstallOnboarding,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Uninstall Onboarding Module (Deferred)")
            }

            OutlinedButton(
                onClick = onUninstallPremium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Uninstall Premium Module (Deferred)")
            }
        }
    }
}
