package com.exapps.omnifetch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exapps.omnifetch.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun getAllByStatus(status: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadEntity): Long

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE downloads SET progress = :progress, speed = :speed, eta = :eta WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Float, speed: String, eta: String)

    @Query("UPDATE downloads SET status = :status, progress = :progress, filePath = :filePath, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompleted(id: Long, status: String, progress: Float, filePath: String, completedAt: Long)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: Long): DownloadEntity?
}
