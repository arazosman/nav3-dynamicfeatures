package com.example.dynamicfeatures.api

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey

/**
 * Type-safe [NavMetadataKey] used to attach [DynamicModule] metadata to a [androidx.navigation3.runtime.NavEntry].
 */
object DynamicModuleMetadataKey : NavMetadataKey<DynamicModule>

data class DynamicModule(
    val entryBuilderClassName: String,
    val moduleName: String,
) {
    @PublishedApi
    internal fun build(scope: EntryProviderScope<NavKey>) {
        val builder = Class.forName(entryBuilderClassName)
            .getConstructor()
            .newInstance() as DynamicModuleEntryBuilder
        with(builder) {
            scope.build()
        }
    }
}

fun interface DynamicModuleEntryBuilder {
    fun EntryProviderScope<NavKey>.build()
}
