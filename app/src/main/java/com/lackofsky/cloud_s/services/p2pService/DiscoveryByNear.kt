package com.lackofsky.cloud_s.services.p2pService

import android.content.Context
import android.os.Looper
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import com.adroitandroid.near.model.Host
import com.lackofsky.cloud_s.data.dao.UserDao

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiscoveryByNear @Inject constructor(private val userDao: UserDao,
                                          private val context: Context) {
    private lateinit var nearDiscovery:NearDiscovery
    private val _hosts = MutableStateFlow<Set<Host>>(mutableSetOf())
//    val hosts: StateFlow<Set<Host>> get() = _hosts.asStateFlow()

    private val _friends = MutableStateFlow<List<HostUser>>(mutableListOf())
    val friends: StateFlow<List<HostUser>> get() = _friends.asStateFlow()
    private val _strangersHosts = MutableStateFlow<List<Host>>(mutableListOf())
    val strangersHosts: StateFlow<List<Host>> get() = _strangersHosts.asStateFlow()

    val db_users = userDao.getAllUsers()
    fun startDiscovery(){
        nearDiscovery = NearDiscovery.Builder()
            .setContext(context)
            .setDiscoverableTimeoutMillis(DISCOVERABLE_TIMEOUT_MILLIS)
            .setDiscoveryTimeoutMillis(DISCOVERY_TIMEOUT_MILLIS)
            .setDiscoverablePingIntervalMillis(DISCOVERABLE_PING_INTERVAL_MILLIS)
            .setDiscoveryListener(nearDiscoveryListener, Looper.getMainLooper())
            .build()
    }
    fun stopDiscovery(){
        nearDiscovery.stopDiscovery()
    }
    fun getDiscovery():NearDiscovery{
        return nearDiscovery
    }

    private val nearDiscoveryListener: NearDiscovery.Listener
        get() = object : NearDiscovery.Listener {
            override fun onPeersUpdate(hosts: Set<Host>) {
                _hosts.value = hosts
                db_users.value?.let { users ->
                    val friendsSet = mutableListOf<HostUser>()
                    val strangersList = mutableListOf<Host>()

                    hosts.asSequence().forEach { host ->
                        users.asSequence().forEach { user ->
                            if (host.name == user.macAddr) {
                                friendsSet.add(HostUser(user = user, host = host))
                            } else {
                                strangersList.add(host)
                                //todo notify who_u_are
                            }
                        }
                    }
                    _friends.value = friendsSet
                    _strangersHosts.value = strangersList
                    //todo тем же образом изменять статус активности пользователей-друзей
                }
            }
            override fun onDiscoveryTimeout() {
                //todo reconect
                // приложение онлайн - пока ищет подключения, и к ней могут подключится
                //todo выдавать уведомление о прошествии времени поиска (пользователи не найдены
//                isDiscovering = false
            }
            override fun onDiscoveryFailure(e: Throwable) {
                //todo
//                Snackbar.make(binding.root,
//                    "Something went wrong while searching for participants",
//                    Snackbar.LENGTH_LONG).show()
            }
            override fun onDiscoverableTimeout() {
                //todo уведомление, что ваша видимость для других пользователей - отключена
            }
        }


    companion object {
        private const val DISCOVERABLE_TIMEOUT_MILLIS: Long = 60000
        private const val DISCOVERY_TIMEOUT_MILLIS: Long = 10000
        private const val DISCOVERABLE_PING_INTERVAL_MILLIS: Long = 5000
    }
}