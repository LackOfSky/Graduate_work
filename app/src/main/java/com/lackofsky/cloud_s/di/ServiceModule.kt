package com.lackofsky.cloud_s.di

import android.content.Context
import com.lackofsky.cloud_s.serviceP2P.P2PClient
import com.lackofsky.cloud_s.serviceP2P.client.ClientInterface
import com.lackofsky.cloud_s.serviceP2P.client.ClientServiceConnectionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {
    @Provides
    fun provideServiceInterface(@ApplicationContext context: Context): ClientInterface {
        // Использование Bound Service для получения интерфейса
        val serviceConnection = ClientServiceConnectionManager(context)
        return serviceConnection.getServiceInterface()
    }
}