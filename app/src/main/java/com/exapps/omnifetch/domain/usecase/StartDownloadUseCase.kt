package com.exapps.omnifetch.domain.usecase

import com.exapps.omnifetch.domain.model.DownloadStatus
import com.exapps.omnifetch.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StartDownloadUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(
        url: String,
        formatId: String,
        title: String,
        thumbnail: String
    ): Flow<DownloadStatus> {
        return repository.startDownload(url, formatId, title, thumbnail)
    }
}
