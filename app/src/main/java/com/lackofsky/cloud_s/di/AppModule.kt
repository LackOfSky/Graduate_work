package com.lackofsky.cloud_s.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.dao.ChatDao
import com.lackofsky.cloud_s.data.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.dao.MessageDao
import com.lackofsky.cloud_s.data.dao.ReadMessageDao
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.database.AppDatabase
import com.lackofsky.cloud_s.data.repository.ChatRepository
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.server.NettyServer
import com.lackofsky.cloud_s.service.server.discovery.WiFiDirectManager
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
    fun provideChatMemberDao(database: AppDatabase): ChatMemberDao {
        return database.chatMemberDao()
    }
    @Provides
    fun provideReadMessageDao(database: AppDatabase): ReadMessageDao {
        return database.readMessageDao()
    }
    @Provides
    @Singleton
    fun provideClientPartP2P(gson: Gson, userRepository: UserRepository,
                             metadata: Metadata
    ): ClientPartP2P {
        return ClientPartP2P(gson, userRepository,metadata)
    }
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    @Provides
    fun provideNettyServer(clientPartP2P: ClientPartP2P,
                           messageRepository: MessageRepository,
                           userRepository: UserRepository,
                           chatRepository: ChatRepository,
                           metadata: Metadata
                           ): NettyServer {//friendResponseUseCase: FriendResponseUseCase
        return NettyServer(clientPartP2P, messageRepository,userRepository, chatRepository, metadata)
    }
    @Provides
    @Singleton
    fun provideMetadata():Metadata{
        return Metadata(15015,"GrimBerry")
    }
    @Provides
    fun provideWiFiDirectManager(@ApplicationContext context: Context, clientPartP2P: ClientPartP2P): WiFiDirectManager {
        return WiFiDirectManager(context,clientPartP2P)
    }

}