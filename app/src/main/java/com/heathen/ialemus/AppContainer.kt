package com.heathen.ialemus

import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.room.Room
import com.heathen.ialemus.core.library.LibraryRepository
import com.heathen.ialemus.core.playlist.PlaylistRepository
import com.heathen.ialemus.core.library.MediaStoreScanner
import com.heathen.ialemus.core.library.SafFolderScanner
import com.heathen.ialemus.core.player.PlayerConnection
import com.heathen.ialemus.core.player.QueueRepository
import com.heathen.ialemus.core.player.ShuffleEngine
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.data.local.IalemusDatabase
import com.heathen.ialemus.data.local.MIGRATION_3_4
import com.heathen.ialemus.data.local.MIGRATION_4_5
import com.heathen.ialemus.core.lyrics.LyricsRepository
import com.heathen.ialemus.core.lyrics.LyricsScanner
import com.heathen.ialemus.core.visualizer.AudioVisualizerController
import com.heathen.ialemus.widget.WidgetStateStore

/**
 * Manual dependency container for MVP 1A.
 * TODO: Replace with Hilt or Koin once DI choice is finalized.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: IalemusDatabase = Room.databaseBuilder(
        appContext,
        IalemusDatabase::class.java,
        "ialemus.db",
    )
        .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
        .fallbackToDestructiveMigration()
        .build()

    val trackDao = database.trackDao()
    val trackStatsDao = database.trackStatsDao()
    val trackOverrideDao = database.trackOverrideDao()
    val librarySourceDao = database.librarySourceDao()
    val playlistDao = database.playlistDao()
    val lyricsDao = database.lyricsDao()

    val settingsRepository = SettingsRepository(appContext)
    val widgetStateStore = WidgetStateStore(appContext)
    val trackOverrideRepository = com.heathen.ialemus.core.library.TrackOverrideRepository(trackOverrideDao)

    val spotifyAuthRepository = com.heathen.ialemus.core.spotify.SpotifyAuthRepository(appContext)
    val spotifyApiClient = com.heathen.ialemus.core.spotify.SpotifyApiClient()
    val spotifyRemoteRepository = com.heathen.ialemus.core.spotify.SpotifyRemoteRepository(appContext)

    private val mediaStoreScanner = MediaStoreScanner(appContext)
    private val safFolderScanner = SafFolderScanner(appContext)
    private val safAccessHelper = com.heathen.ialemus.core.library.SafAccessHelper(appContext)
    val playlistRepository = PlaylistRepository(
        playlistDao = playlistDao,
        trackDao = trackDao,
    )

    val lyricsScanner = LyricsScanner(appContext)
    val lyricsRepository = LyricsRepository(lyricsDao, lyricsScanner)
    val audioVisualizerController = AudioVisualizerController()

    val libraryRepository = LibraryRepository(
        context = appContext,
        mediaStoreScanner = mediaStoreScanner,
        safFolderScanner = safFolderScanner,
        safAccessHelper = safAccessHelper,
        trackDao = trackDao,
        trackStatsDao = trackStatsDao,
        trackOverrideDao = trackOverrideDao,
        librarySourceDao = librarySourceDao,
        settingsRepository = settingsRepository,
    )

    private val shuffleEngine = ShuffleEngine()
    val queueRepository = QueueRepository(shuffleEngine)
    val playerConnection = PlayerConnection(
        context = appContext,
        queueRepository = queueRepository,
        shuffleEngine = shuffleEngine,
        widgetStateStore = widgetStateStore,
    )

    fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
}
