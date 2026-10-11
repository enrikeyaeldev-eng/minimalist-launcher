package dev.minimalist.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import java.text.Collator

/** Reads launchable apps from PackageManager. Call off the main thread. */
class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    fun loadApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val collator = Collator.getInstance()
        return query(intent)
            .map { it.toEntry() }
            .filter { it.packageName != context.packageName }
            .sortedWith(compareBy(collator) { it.label })
    }

    @Suppress("DEPRECATION") // The flags overload is API 33+; this one works everywhere.
    private fun query(intent: Intent): List<ResolveInfo> = pm.queryIntentActivities(intent, 0)

    private fun ResolveInfo.toEntry(): AppEntry {
        val info = activityInfo
        return AppEntry(
            label = info.loadLabel(pm).toString().trim(),
            component = ComponentName(info.packageName, info.name),
            isSystem = info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0,
        )
    }
}
