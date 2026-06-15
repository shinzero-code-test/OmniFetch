package com.exapps.omnifetch.domain.usecase

import com.exapps.omnifetch.domain.model.DownloadItem
import com.exapps.omnifetch.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadHistoryUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(): Flow<List<DownloadItem>> {
        return repository.getDownloadHistory()
    }
}
