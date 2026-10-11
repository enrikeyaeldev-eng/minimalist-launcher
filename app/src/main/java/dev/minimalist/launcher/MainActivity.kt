package dev.minimalist.launcher

import android.content.ActivityNotFoundException
import android.graphics.Color as AndroidColor
import android.os.Bundle
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
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Light status and navigation bar icons on a transparent bar, regardless of system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        repository = AppRepository(applicationContext)
        reload()

        setContent {
            // This is the home screen: there is nothing to go back to.
            BackHandler {}
            LauncherScreen(home = home, onOpen = ::open)
        }
    }

    private fun reload() {
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            home = withContext(Dispatchers.Default) { repository.load() }
        }
    }

    private fun open(app: AppEntry) {
        try {
            startActivity(app.launchIntent())
        } catch (e: ActivityNotFoundException) {
            // Removed or disabled since the list was built.
            reload()
        } catch (e: SecurityException) {
            reload()
        }
    }
}
