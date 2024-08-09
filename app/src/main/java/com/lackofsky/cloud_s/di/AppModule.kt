package com.lackofsky.cloud_s.di

import android.content.Context
import androidx.room.Room
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.database.AppDatabase
import com.lackofsky.cloud_s.services.p2pService.P2pByNear
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
@Provides
@Singleton
fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
    return Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "app_database"
    ).build()
}
    @Provides
    @Singleton
    fun provideP2pByNear(@ApplicationContext appContext: Context): P2pByNear {
        return P2pByNear(appContext)
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }


}