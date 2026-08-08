package com.bluemarlin.drinkdiary.debug

import android.content.Context
import com.bluemarlin.drinkdiary.AppContainer

// Release no-op counterpart to app/src/debug/java/.../debug/DebugSeeder.kt. Kotlin source
// sets are additive per variant (main + debug, or main + release) rather than override-
// based, so this class must live in exactly one variant-specific source set — not in
// `main` — to avoid a duplicate declaration when compiling the debug variant.
object DebugSeeder {
    suspend fun seedIfNeeded(context: Context, appContainer: AppContainer) {}
}
