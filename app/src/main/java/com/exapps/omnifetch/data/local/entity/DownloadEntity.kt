package com.exapps.omnifetch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exapps.omnifetch.domain.model.DownloadItem
import com.exapps.omnifetch.domain.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String = "",
    val thumbnail: String = "",
    val url: String = "",
    val status: String = "idle",
    val progress: Float = 0f,
    val filePath: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val fileSize: Long? = null,
    val formatId: String = "",
    val extension: String = "",
    val speed: String = "",
    val eta: String = ""
) {
    fun toDomain(): DownloadItem {
        return DownloadItem(
            id = id,
            title = title,
            thumbnail = thumbnail,
            url = url,
            status = DownloadStatus.fromString(status, progress, speed, eta, filePath = filePath),
            progress = progress,
            filePath = filePath,
            createdAt = createdAt,
            completedAt = completedAt,
            fileSize = fileSize,
            formatId = formatId,
            extension = extension,
            speed = speed,
            eta = eta
        )
    }

    companion object {
        fun fromDomain(item: DownloadItem): DownloadEntity {
            return DownloadEntity(
                id = item.id,
                title = item.title,
                thumbnail = item.thumbnail,
                url = item.url,
                status = item.status.toSimpleString(),
                progress = item.progress,
                filePath = item.filePath,
                createdAt = item.createdAt,
                completedAt = item.completedAt,
                fileSize = item.fileSize,
                formatId = item.formatId,
                extension = item.extension,
                speed = item.speed,
                eta = item.eta
            )
        }
    }
}
