package com.heathen.ialemus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heathen.ialemus.data.local.dao.TrackDao
import com.heathen.ialemus.data.local.dao.TrackStatsDao
import com.heathen.ialemus.data.local.entity.TrackEntity
import com.heathen.ialemus.data.local.entity.TrackStatsEntity

@Database(
    entities = [TrackEntity::class, TrackStatsEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class IalemusDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackStatsDao(): TrackStatsDao
}
