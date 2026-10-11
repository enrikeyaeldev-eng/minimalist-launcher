package dev.minimalist.launcher

import android.content.ComponentName
import android.content.Intent

/** One launchable activity, as shown in the list. */
data class AppEntry(
    val label: String,
    val component: ComponentName,
    /** Preinstalled system app: cannot be uninstalled. */
    val isSystem: Boolean,
) {
    val packageName: String get() = component.packageName

    /** Stable list key. A package can have more than one launchable activity. */
    val key: String get() = component.flattenToShortString()

    fun launchIntent(): Intent =
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
}
