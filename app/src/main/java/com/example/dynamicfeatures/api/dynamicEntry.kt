package com.example.dynamicfeatures.api

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.metadata

/**
 * Registers a dynamic feature destination [K] as an entry with [DynamicModule] metadata attached.
 * The destination rendering and deferred class loading are automatically handled by [dynamicFeatureDecorator].
 */
inline fun <reified K : NavKey> EntryProviderScope<in K>.dynamicEntry(
    noinline clazzContentKey: (key: @JvmSuppressWildcards K) -> Any = { it },
    metadata: Map<String, Any> = emptyMap(),
) {
    val resolvedModule = runCatching {
        val outerObj = (K::class.java.enclosingClass ?: K::class.java.declaringClass)?.getDeclaredField("INSTANCE")?.get(null)
        (outerObj as? DynamicModule) ?: (outerObj?.javaClass?.getMethod("getModule")?.invoke(outerObj) as? DynamicModule)
    }.getOrNull()
        ?: error("Could not resolve DynamicModule for ${K::class.java.simpleName}. Make sure it is defined within an object providing a DynamicModule.")

    val dynamicMetadata = metadata {
        put(DynamicModuleMetadataKey, resolvedModule)
    }

    entry<K>(
        clazzContentKey = clazzContentKey,
        metadata = metadata + dynamicMetadata,
        content = {}, // Content rendering is deferred and managed by dynamicFeatureDecorator
    )
}
