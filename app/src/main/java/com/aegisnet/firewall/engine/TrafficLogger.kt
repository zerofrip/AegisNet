package com.aegisnet.firewall.engine

import com.aegisnet.database.dao.ConnectionLogDao
import com.aegisnet.database.dao.TrafficStatsDao
import com.aegisnet.database.entity.ConnectionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrafficLogger @Inject constructor(
    private val trafficStatsDao: TrafficStatsDao,
    private val connectionLogDao: ConnectionLogDao
) {
    // Channel for buffering connection logs before DB insert
    private val logChannel = Channel<ConnectionLog>(capacity = 1000)
    
    // In-memory buffer for traffic stats (UID -> [Upload, Download, Count])
    private val statsBuffer = mutableMapOf<Int, LongArray>()
    private val statsLock = Any()

    init {
        // Coroutine to flush logs to DB sequentially
        CoroutineScope(Dispatchers.IO).launch {
            for (log in logChannel) {
                connectionLogDao.insert(log)
            }
        }

        // Coroutine to periodically flush traffic stats to DB
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(5000) // Flush every 5 seconds
                flushStats()
            }
        }
    }

    fun logConnection(uid: Int, domain: String, ip: String, action: String) {
        val log = ConnectionLog(
            timestamp = System.currentTimeMillis(),
            appUid = uid,
            domain = domain,
            ip = ip,
            action = action
        )
        // Non-blocking send; if buffer is full, we drop the log rather than blocking the network thread
        logChannel.trySend(log)
    }

    fun recordTraffic(uid: Int, uploadBytes: Long, downloadBytes: Long) {
        synchronized(statsLock) {
            val stats = statsBuffer.getOrPut(uid) { LongArray(3) }
            stats[0] = stats[0] + uploadBytes
            stats[1] = stats[1] + downloadBytes
            stats[2] = stats[2] + 1L // Connection count
        }
    }

    private suspend fun flushStats() {
        val snapshot: Map<Int, LongArray>
        synchronized(statsLock) {
            if (statsBuffer.isEmpty()) return
            snapshot = statsBuffer.toMap()
            statsBuffer.clear()
        }

        snapshot.forEach { (uid, stats) ->
            trafficStatsDao.addTrafficStats(
                appUid = uid,
                up = stats[0],
                down = stats[1],
                conn = stats[2]
            )
        }
    }
}
