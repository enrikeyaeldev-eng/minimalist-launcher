package dev.minimalist.launcher

import android.content.ComponentName
import android.content.Intent

/** One launchable activity, as shown in the list. */
data class AppEntry(
    val label: String,
    val component: ComponentName,
    /** Preinstalled system app: cannot be uninstalled. */
    val isSystem: Boolean,
    /**
     * Normally MAIN. The camera falls back to its capture action when the
     * camera app has no launcher entry of its own.
     */
    val action: String = Intent.ACTION_MAIN,
) {
    val packageName: String get() = component.packageName

    /** Stable list key. A package can have more than one launchable activity. */
    val key: String get() = component.flattenToShortString()

    fun launchIntent(): Intent =
        Intent(action)
            .apply { if (action == Intent.ACTION_MAIN) addCategory(Intent.CATEGORY_LAUNCHER) }
            .setComponent(component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
}
