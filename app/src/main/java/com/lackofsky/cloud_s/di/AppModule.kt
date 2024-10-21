package com.lackofsky.cloud_s.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.lackofsky.cloud_s.data.dao.ChatDao
import com.lackofsky.cloud_s.data.dao.MessageDao
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.database.AppDatabase
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.data.SharedState
import com.lackofsky.cloud_s.service.server.NettyServer
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
    )   .fallbackToDestructiveMigration()
        .build()
}

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }
    @Provides
    @Singleton
    fun provideSharedState(): SharedState {
        return SharedState()
    }
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    @Provides
    fun provideNettyServer(sharedState: SharedState,
                           messageRepository: MessageRepository,
                           userRepository: UserRepository): NettyServer {
        return NettyServer(sharedState, messageRepository,userRepository)
    }
//    @Provides
//    @Singleton
//    fun provideDiscoveryByNear(@ApplicationContext context: Context,userDao: UserDao):DiscoveryByNear{
//        return DiscoveryByNear(userDao,context)
//    }
//    @Provides
//    @Singleton
//    fun provideConnectByNear(@ApplicationContext context: Context,userRepository: UserRepository): ConnectByNear {
//        return ConnectByNear(context,userRepository)
//    }
//    @Provides
//    @Singleton
//    fun provideNear(discovery:DiscoveryByNear,connectByNear: ConnectByNear): Near {
//        return Near(discovery,connectByNear)
//    }

//@Provides
//fun provideClientServiceInterface(@ApplicationContext context: Context): ClientInterface {
//    // Использование Bound Service для получения интерфейса
//    val serviceConnection = ClientServiceConnectionManager(context)
//    return serviceConnection.getServiceInterface()
//}

}