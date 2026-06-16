package com.heathen.ialemus

import android.content.Context
import androidx.room.Room
import com.heathen.ialemus.core.library.LibraryRepository
import com.heathen.ialemus.core.library.MediaStoreScanner
import com.heathen.ialemus.core.library.SafFolderScanner
import com.heathen.ialemus.core.player.PlayerConnection
import com.heathen.ialemus.core.player.QueueRepository
import com.heathen.ialemus.core.player.ShuffleEngine
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.data.local.IalemusDatabase
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
        .fallbackToDestructiveMigration()
        .build()

    val trackDao = database.trackDao()
    val trackStatsDao = database.trackStatsDao()
    val trackOverrideDao = database.trackOverrideDao()
    val librarySourceDao = database.librarySourceDao()

    val settingsRepository = SettingsRepository(appContext)
    val widgetStateStore = WidgetStateStore(appContext)
    val trackOverrideRepository = com.heathen.ialemus.core.library.TrackOverrideRepository(trackOverrideDao)

    val spotifyAuthRepository = com.heathen.ialemus.core.spotify.SpotifyAuthRepository(appContext)
    val spotifyApiClient = com.heathen.ialemus.core.spotify.SpotifyApiClient()

    private val mediaStoreScanner = MediaStoreScanner(appContext)
    private val safFolderScanner = SafFolderScanner(appContext)
    val libraryRepository = LibraryRepository(
        context = appContext,
        mediaStoreScanner = mediaStoreScanner,
        safFolderScanner = safFolderScanner,
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
}
