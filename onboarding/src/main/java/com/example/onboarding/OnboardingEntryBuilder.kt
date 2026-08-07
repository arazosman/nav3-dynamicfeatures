package com.example.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.DynamicModuleEntryBuilder
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.dynamicfeatures.OnboardingKey
import com.example.dynamicfeatures.api.LocalNavBackStack
import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingNavKey : NavKey {
    @Serializable data object AddressInfo : OnboardingNavKey
    @Serializable data class Confirmation(val name: String = "Alex", val city: String = "San Francisco") : OnboardingNavKey
}

class OnboardingEntryBuilder : DynamicModuleEntryBuilder {

    override fun EntryProviderScope<NavKey>.build() {

        // Screen 1: Basic Info (Entry Screen)
        entry<OnboardingKey> {
            val backStack = LocalNavBackStack.current
            BasicInfoScreen(
                onProceedToAddress = { name ->
                    backStack.add(OnboardingNavKey.AddressInfo)
                },
                onCancel = {
                    backStack.removeAll { it is OnboardingNavKey || it == OnboardingKey }
                }
            )
        }

        // Screen 2: Address Info (Step 2)
        entry<OnboardingNavKey.AddressInfo> {
            val backStack = LocalNavBackStack.current
            AddressInfoScreen(
                onProceedToConfirmation = { city ->
                    backStack.add(OnboardingNavKey.Confirmation(name = "Alex", city = city))
                },
                onBack = {
                    backStack.removeLastOrNull()
                }
            )
        }

        // Screen 3: Confirmation (Step 3)
        entry<OnboardingNavKey.Confirmation> { key ->
            val backStack = LocalNavBackStack.current
            ConfirmationScreen(
                name = key.name,
                city = key.city,
                onComplete = {
                    backStack.removeAll { it is OnboardingNavKey || it == OnboardingKey }
                }
            )
        }
    }
}

@Composable
private fun BasicInfoScreen(
    onProceedToAddress: (String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("Alex Morgan") }
    var email by remember { mutableStateOf("alex.morgan@example.com") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👤",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Step 1: Personal Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Welcome to the multi-screen Onboarding Dynamic Feature!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onProceedToAddress(name) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next: Address Info (Step 2/3)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Onboarding")
            }
        }
    }
}

@Composable
private fun AddressInfoScreen(
    onProceedToConfirmation: (String) -> Unit,
    onBack: () -> Unit
) {
    var street by remember { mutableStateOf("100 Market Street") }
    var city by remember { mutableStateOf("San Francisco") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📍",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Step 2: Address Information",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Demonstrating 0ms instant transition within dynamic module.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Street Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onProceedToConfirmation(city) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next: Review & Confirm (Step 3/3)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Step 1")
            }
        }
    }
}

@Composable
private fun ConfirmationScreen(
    name: String,
    city: String,
    onComplete: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Step 3: Onboarding Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: $name", style = MaterialTheme.typography.bodyMedium)
                    Text("City: $city", style = MaterialTheme.typography.bodyMedium)
                    Text("Module: :onboarding (Play Feature Delivery)", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finish & Exit Onboarding")
            }
        }
    }
}
