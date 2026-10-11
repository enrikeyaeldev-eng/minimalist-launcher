package dev.minimalist.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.MediaStore
import java.text.Collator

/** What the home screen shows: the pinned camera, then everything else. */
data class HomeApps(
    val camera: AppEntry?,
    val apps: List<AppEntry>,
)

/** Reads launchable apps from PackageManager. Call off the main thread. */
class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    fun load(): HomeApps {
        val all = loadApps()
        val cameraActivity = findCameraActivity() ?: return HomeApps(camera = null, apps = all)

        // Prefer the camera app's own launcher entry, so it opens the same way as from any launcher.
        val cameraEntry = all.firstOrNull { it.packageName == cameraActivity.activityInfo.packageName }
            ?: cameraActivity.toEntry().copy(action = MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)

        return HomeApps(
            camera = cameraEntry,
            apps = all.filter { it.packageName != cameraEntry.packageName },
        )
    }

    private fun loadApps(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val collator = Collator.getInstance()
        return query(intent)
            .map { it.toEntry() }
            .filter { it.packageName != context.packageName }
            .sortedWith(compareBy(collator) { it.label })
    }

    /**
     * The device's default camera, resolved at runtime from the "open camera" intent.
     * If the user hasn't picked a default, Android resolves to its chooser instead;
     * then we take the preinstalled camera, or any camera if none is preinstalled.
     */
    private fun findCameraActivity(): ResolveInfo? {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        val candidates = query(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (candidates.isEmpty()) return null

        @Suppress("DEPRECATION")
        val default = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        // The chooser is not one of the candidates, so this also rules it out.
        candidates.firstOrNull { it.activityInfo.packageName == default?.activityInfo?.packageName }
            ?.let { return it }

        return candidates.firstOrNull { it.activityInfo.applicationInfo.isSystemApp() }
            ?: candidates.first()
    }

    @Suppress("DEPRECATION") // The flags overload is API 33+; this one works everywhere.
    private fun query(intent: Intent, flags: Int = 0): List<ResolveInfo> =
        pm.queryIntentActivities(intent, flags)

    private fun ResolveInfo.toEntry(): AppEntry {
        val info = activityInfo
        return AppEntry(
            label = info.loadLabel(pm).toString().trim(),
            component = ComponentName(info.packageName, info.name),
            isSystem = info.applicationInfo.isSystemApp(),
        )
    }

    private fun ApplicationInfo.isSystemApp() = flags and ApplicationInfo.FLAG_SYSTEM != 0
}
