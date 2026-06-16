package com.heathen.ialemus

import android.content.Context
import androidx.room.Room
import com.heathen.ialemus.core.library.LibraryRepository
import com.heathen.ialemus.core.library.MediaStoreScanner
import com.heathen.ialemus.core.player.PlayerConnection
import com.heathen.ialemus.core.player.QueueRepository
import com.heathen.ialemus.core.player.ShuffleEngine
import com.heathen.ialemus.core.settings.SettingsRepository
import com.heathen.ialemus.data.local.IalemusDatabase

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
        // Early MVP: destructive migration acceptable until schema stabilizes.
        .fallbackToDestructiveMigration()
        .build()

    val trackDao = database.trackDao()
    val trackStatsDao = database.trackStatsDao()

    private val mediaStoreScanner = MediaStoreScanner(appContext)
    val libraryRepository = LibraryRepository(
        context = appContext,
        scanner = mediaStoreScanner,
        trackDao = trackDao,
        trackStatsDao = trackStatsDao,
    )

    val settingsRepository = SettingsRepository(appContext)

    private val shuffleEngine = ShuffleEngine()
    val queueRepository = QueueRepository(shuffleEngine)
    val playerConnection = PlayerConnection(
        context = appContext,
        queueRepository = queueRepository,
        shuffleEngine = shuffleEngine,
    )
}
