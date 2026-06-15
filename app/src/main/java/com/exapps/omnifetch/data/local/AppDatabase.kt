package com.exapps.omnifetch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.exapps.omnifetch.data.local.dao.DownloadDao
import com.exapps.omnifetch.data.local.entity.DownloadEntity

@Database(
    entities = [DownloadEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
