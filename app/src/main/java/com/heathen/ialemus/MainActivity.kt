package com.heathen.ialemus

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.core.spotify.SpotifyViewModel
import com.heathen.ialemus.ui.IalemusApp
import com.heathen.ialemus.ui.util.isSpotifyAuthCallback

class MainActivity : ComponentActivity() {
    private val container by lazy { (application as IalemusApplication).container }

    private val libraryViewModel: LibraryViewModel by viewModels {
        LibraryViewModel.Factory(container)
    }
    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModel.Factory(container)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(container)
    }
    private val spotifyViewModel: SpotifyViewModel by viewModels {
        SpotifyViewModel.Factory(container)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* optional for MVP 1A */ }

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        settingsViewModel.setReactiveVisualizerEnabled(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // MediaController.connect() starts/binds MediaSessionService; do not startForegroundService here.
        playerViewModel.connect()
        handleSpotifyCallback(intent)

        setContent {
            IalemusApp(
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                settingsViewModel = settingsViewModel,
                spotifyViewModel = spotifyViewModel,
                onRequestNotificationPermission = ::requestNotificationPermissionIfNeeded,
                onRequestRecordAudioPermission = ::requestRecordAudioForVisualizer,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSpotifyCallback(intent)
    }

    override fun onDestroy() {
        if (isFinishing) {
            playerViewModel.disconnect()
        }
        super.onDestroy()
    }

    private fun handleSpotifyCallback(intent: Intent?) {
        val uri = intent?.data ?: return
        if (isSpotifyAuthCallback(uri)) {
            spotifyViewModel.handleAuthCallback(uri)
            intent.data = null
        }
    }

    private fun requestRecordAudioForVisualizer() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            settingsViewModel.setReactiveVisualizerEnabled(true)
            return
        }
        recordAudioPermissionLauncher.launch(permission)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(permission)
        }
    }
}
