package com.example.dynamicfeatures.api

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * [androidx.compose.runtime.CompositionLocal] providing the active [NavBackStack] to all composable screens,
 * allowing dynamic feature modules to perform backstack navigation without tight constructor coupling.
 */
val LocalNavBackStack = staticCompositionLocalOf<NavBackStack<NavKey>> {
    error("No NavBackStack provided in the current composition hierarchy.")
}
