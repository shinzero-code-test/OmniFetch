package com.exapps.omnifetch.domain.usecase

import com.exapps.omnifetch.domain.model.VideoInfo
import com.exapps.omnifetch.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetVideoMetadataUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(url: String): Flow<Result<VideoInfo>> = flow {
        emit(repository.fetchVideoInfo(url))
    }
}
