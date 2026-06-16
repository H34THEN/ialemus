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
import com.heathen.ialemus.core.library.LibraryViewModel
import com.heathen.ialemus.core.player.IalemusPlaybackService
import com.heathen.ialemus.core.player.PlayerViewModel
import com.heathen.ialemus.core.settings.SettingsViewModel
import com.heathen.ialemus.ui.IalemusApp

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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* optional for MVP 1A */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startPlaybackService()
        playerViewModel.connect()

        setContent {
            IalemusApp(
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                settingsViewModel = settingsViewModel,
                onRequestNotificationPermission = ::requestNotificationPermissionIfNeeded,
            )
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            playerViewModel.disconnect()
        }
        super.onDestroy()
    }

    private fun startPlaybackService() {
        val intent = Intent(this, IalemusPlaybackService::class.java)
        ContextCompat.startForegroundService(this, intent)
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
