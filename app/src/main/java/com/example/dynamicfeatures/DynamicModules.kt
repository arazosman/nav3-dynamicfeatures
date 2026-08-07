package com.example.dynamicfeatures

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

const val ONBOARDING_MODULE_NAME = "onboarding"
const val PREMIUM_MODULE_NAME = "premium"

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
