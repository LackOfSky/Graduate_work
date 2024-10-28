package com.lackofsky.cloud_s.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.dao.ChatDao
import com.lackofsky.cloud_s.data.dao.MessageDao
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.database.AppDatabase
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
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
    @Singleton
    fun provideGson():Gson{
        return Gson()
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
    fun provideClientPartP2P(gson: Gson, userRepository: UserRepository): ClientPartP2P {
        return ClientPartP2P(gson, userRepository)
    }
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    @Provides
    fun provideNettyServer(clientPartP2P: ClientPartP2P,
                           messageRepository: MessageRepository,
                           userRepository: UserRepository): NettyServer {
        return NettyServer(clientPartP2P, messageRepository,userRepository)
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