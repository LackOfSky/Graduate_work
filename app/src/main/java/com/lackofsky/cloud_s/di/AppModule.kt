package com.lackofsky.cloud_s.di

import android.app.NotificationManager
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import androidx.room.Room
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.database.dao.ChatDao
import com.lackofsky.cloud_s.data.database.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.database.dao.MessageDao
import com.lackofsky.cloud_s.data.database.dao.ReadMessageDao
import com.lackofsky.cloud_s.data.database.dao.UserDao
import com.lackofsky.cloud_s.data.database.AppDatabase
import com.lackofsky.cloud_s.data.database.repository.ChatMemberRepository
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.storage.StorageRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaServer
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaClient
import com.lackofsky.cloud_s.service.server.MediaDispatcher
import com.lackofsky.cloud_s.service.server.NettyServer
import com.lackofsky.cloud_s.service.server.discovery.DirectDiscoveryManager
import com.lackofsky.cloud_s.service.server.discovery.DirectGroupManager
import com.lackofsky.cloud_s.service.server.discovery.NSDManager
import com.lackofsky.cloud_s.service.server.discovery.WiFiDirectService
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
    fun provideClientPartP2P(gson: Gson, userRepository: UserRepository, metadata: Metadata
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
                           metadata: Metadata,
                           mediaDispatcher: MediaDispatcher,
                           mediaServer: NettyMediaServer,
                           mediaClient: NettyMediaClient
                           ): NettyServer {//friendResponseUseCase: FriendResponseUseCase
        return NettyServer(clientPartP2P, messageRepository,userRepository, chatRepository, metadata, mediaDispatcher, mediaServer, mediaClient)
    }
    @Provides
    fun provideNettyMediaServer(@ApplicationContext context: Context,
                                metadata: Metadata,
                                mediaDispatcher: MediaDispatcher,
                                userRepository: UserRepository,
                                messageRepository: MessageRepository): NettyMediaServer {
        return NettyMediaServer(context, metadata,mediaDispatcher, userRepository, messageRepository)
    }
    @Provides
    fun provideNettyMediaClient(@ApplicationContext context: Context,
                                userRepository: UserRepository,
                                messageRepository: MessageRepository): NettyMediaClient{
        return NettyMediaClient(context,userRepository, messageRepository)
    }
    @Provides @Singleton
    fun provideMediaDispatcher(): MediaDispatcher {return MediaDispatcher()}
    @Provides
    @Singleton
    fun provideMetadata():Metadata{
        return Metadata(15015,"GrimBerry")
    }
//    @Provides //TODO( DEPRECATED )
//    fun provideWiFiDirectManager(@ApplicationContext context: Context, clientPartP2P: ClientPartP2P): WiFiDirectManager {
//        return WiFiDirectManager(context,clientPartP2P)
//    }
    @Provides
    @Singleton
    fun provideWiFiDirectService(directGroupManager: DirectGroupManager,
                                 directDiscoveryManager: DirectDiscoveryManager,
                                 nsdManager: NSDManager): WiFiDirectService {
        return WiFiDirectService(directGroupManager,directDiscoveryManager,nsdManager)
    }
//    @Provides //TODO("not implement android system")
//    fun provideWiFiDiscoveryByAware(@ApplicationContext context: Context, clientPartP2P: ClientPartP2P): WiFiDiscoveryByAware {
//        return WiFiDiscoveryByAware(context,clientPartP2P)
//    }

    @Provides
    @Singleton
    fun provideChatMemberRepository(chatMemberDao: ChatMemberDao): ChatMemberRepository {
        return ChatMemberRepository(chatMemberDao)
    }
    @Provides
    @Singleton
    fun provideStorageRepository():StorageRepository{
        return StorageRepository()
    }

    @Provides
    @Singleton
    fun provideWifiP2pManager(@ApplicationContext context: Context): WifiP2pManager {
        return context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    @Provides
    @Singleton
    fun provideWifiP2pChannel(@ApplicationContext context: Context): WifiP2pManager.Channel {
        return provideWifiP2pManager(context).initialize(context, context.mainLooper, null)
    }
    @Provides
    @Singleton
    fun provideDirectGroupManager(@ApplicationContext context: Context, clientPartP2P: ClientPartP2P,
                                  wifiP2pManager: WifiP2pManager,
                                  wifiP2pChannel:WifiP2pManager.Channel): DirectGroupManager {
        return DirectGroupManager(context,clientPartP2P,wifiP2pManager,wifiP2pChannel)
    }
    @Provides
    @Singleton
    fun provideDirectDiscoveryManager(@ApplicationContext context: Context, clientPartP2P: ClientPartP2P,
                                  wifiP2pManager: WifiP2pManager,
                                  wifiP2pChannel:WifiP2pManager.Channel): DirectDiscoveryManager {
        return DirectDiscoveryManager(context,clientPartP2P,wifiP2pManager,wifiP2pChannel)
    }

    @Provides
    @Singleton
    fun provideNSDManager(@ApplicationContext context: Context, clientPartP2P: ClientPartP2P): NSDManager {
        return NSDManager(context,clientPartP2P)
    }
}