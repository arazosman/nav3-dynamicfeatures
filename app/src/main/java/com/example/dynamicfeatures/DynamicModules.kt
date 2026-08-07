package com.example.dynamicfeatures

import androidx.navigation3.DynamicModule
import androidx.navigation3.runtime.NavKey
import com.example.dynamicfeatures.api.LocalNavBackStack
import kotlinx.serialization.Serializable

/**
 * Initial destination [NavKey] for the Onboarding dynamic feature module,
 * defined in the common app module.
 */
@Serializable
data object OnboardingKey : NavKey

/**
 * Initial destination [NavKey] for the Premium dynamic feature module,
 * defined in the common app module.
 */
@Serializable
data object PremiumKey : NavKey

/**
 * Multi-screen Onboarding Dynamic Feature Module with custom loading screen.
 */
val OnboardingModule = DynamicModule(
    initialKey = OnboardingKey,
    entryBuilderClassName = "com.example.onboarding.OnboardingEntryBuilder",
    moduleName = "onboarding",
    loadingContent = { sessionState ->
        val backStack = LocalNavBackStack.current
        DynamicFeatureProgressScreen(
            moduleName = "Onboarding Feature (Custom Module UI)",
            sessionState = sessionState,
            onCancel = { backStack.removeLastOrNull() }
        )
    }
)

/**
 * Single-screen Premium Dynamic Feature Module.
 */
val PremiumModule = DynamicModule(
    initialKey = PremiumKey,
    entryBuilderClassName = "com.example.premium.PremiumEntryBuilder",
    moduleName = "premium",
    loadingContent = null
)
