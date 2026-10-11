package dev.minimalist.launcher

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var repository: AppRepository
    private var home by mutableStateOf(HomeApps(camera = null, apps = emptyList()))
    private var heldKey by mutableStateOf<String?>(null)
    private var loadJob: Job? = null

    /** Reloads the list whenever an app is installed, removed, updated, enabled or disabled. */
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = reload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Light status and navigation bar icons on a transparent bar, regardless of system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        repository = AppRepository(applicationContext)

        // These are protected system broadcasts, so no export flag is needed
        // and no other app can send them.
        registerReceiver(
            packageReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            },
        )

        setContent {
            // This is the home screen: there is nothing to go back to.
            // Back only dismisses a long-press, if one is showing.
            BackHandler { heldKey = null }
            LauncherScreen(
                home = home,
                heldKey = heldKey,
                onOpen = ::open,
                onHold = { heldKey = it.key },
                onRelease = { heldKey = null },
                onRemove = ::remove,
            )
        }
    }

    override fun onStart() {
        super.onStart()
        // Also catches changes no broadcast reports, such as a new default camera.
        reload()
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        // Come back to a clean list after opening an app or the uninstall dialog.
        heldKey = null
    }

    private fun reload() {
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            home = withContext(Dispatchers.Default) { repository.load() }
        }
    }

    private fun open(app: AppEntry) = start(app.launchIntent())

    /** System uninstall dialog, or App Info for system apps that can't be uninstalled. */
    private fun remove(app: AppEntry) {
        heldKey = null
        val uri = Uri.fromParts("package", app.packageName, null)
        val action = if (app.isSystem) Settings.ACTION_APPLICATION_DETAILS_SETTINGS else Intent.ACTION_DELETE
        start(Intent(action, uri))
    }

    private fun start(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // The app was removed or disabled since the list was built.
            reload()
        } catch (e: SecurityException) {
            reload()
        }
    }
}
