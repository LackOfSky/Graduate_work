package com.lackofsky.cloud_s.di

import com.lackofsky.cloud_s.data.LocalDataSource
import com.lackofsky.cloud_s.data.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
//    @Provides
//    @Singleton
//    fun provideRepository(localDataSource: LocalDataSource): Repository {
//        return Repository(localDataSource)
//    }

}