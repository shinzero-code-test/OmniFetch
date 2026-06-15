package com.exapps.omnifetch.di

import android.content.Context
import com.exapps.omnifetch.data.local.AppDatabase
import com.exapps.omnifetch.data.local.dao.DownloadDao
import com.exapps.omnifetch.data.remote.YtDlpDataSource
import com.exapps.omnifetch.data.repository.DownloadRepositoryImpl
import com.exapps.omnifetch.domain.repository.DownloadRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDownloadDao(database: AppDatabase): DownloadDao = database.downloadDao()

    @Provides
    @Singleton
    fun provideYtDlpDataSource(context: Context): YtDlpDataSource = YtDlpDataSource(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository
}
