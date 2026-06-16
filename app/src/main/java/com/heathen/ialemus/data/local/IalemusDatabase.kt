package com.heathen.ialemus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heathen.ialemus.data.local.dao.LibrarySourceDao
import com.heathen.ialemus.data.local.dao.TrackDao
import com.heathen.ialemus.data.local.dao.TrackOverrideDao
import com.heathen.ialemus.data.local.dao.TrackStatsDao
import com.heathen.ialemus.data.local.entity.LibrarySourceEntity
import com.heathen.ialemus.data.local.entity.TrackEntity
import com.heathen.ialemus.data.local.entity.TrackOverrideEntity
import com.heathen.ialemus.data.local.entity.TrackStatsEntity

@Database(
    entities = [
        TrackEntity::class,
        TrackStatsEntity::class,
        LibrarySourceEntity::class,
        TrackOverrideEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class IalemusDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackStatsDao(): TrackStatsDao
    abstract fun librarySourceDao(): LibrarySourceDao
    abstract fun trackOverrideDao(): TrackOverrideDao
}
