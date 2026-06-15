package com.exapps.omnifetch.domain.usecase

import com.exapps.omnifetch.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateYtDlpUseCase @Inject constructor(
    private val repository: DownloadRepository
) {
    operator fun invoke(): Flow<Result<Unit>> {
        return repository.updateYtDlp()
    }
}
