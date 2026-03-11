package com.aegisnet.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aegisnet.database.entity.AppInfo
import com.aegisnet.database.entity.AppDomainRule
import com.aegisnet.database.entity.AppDNSRule
import com.aegisnet.database.entity.AppRoutingRule
import com.aegisnet.database.entity.TrafficStats
import com.aegisnet.database.entity.ConnectionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM applications")
    fun getAll(): Flow<List<AppInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppInfo>)
    
    @Query("DELETE FROM applications WHERE uid NOT IN (:uids)")
    suspend fun deleteRemovedApps(uids: List<Int>)
}

@Dao
interface AppDomainRuleDao {
    @Query("SELECT * FROM app_domain_rules WHERE appUid = :appUid")
    fun getRulesForApp(appUid: Int): Flow<List<AppDomainRule>>

    @Query("SELECT * FROM app_domain_rules")
    fun getAllRules(): Flow<List<AppDomainRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AppDomainRule)

    @Delete
    suspend fun delete(rule: AppDomainRule)
}

@Dao
interface AppDNSRuleDao {
    @Query("SELECT * FROM app_dns_rules WHERE appUid = :appUid")
    fun getRuleForApp(appUid: Int): Flow<AppDNSRule?>

    @Query("SELECT * FROM app_dns_rules")
    fun getAllRules(): Flow<List<AppDNSRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AppDNSRule)

    @Delete
    suspend fun delete(rule: AppDNSRule)
}

@Dao
interface AppRoutingRuleDao {
    @Query("SELECT * FROM app_routing_rules WHERE appUid = :appUid")
    fun getRuleForApp(appUid: Int): Flow<AppRoutingRule?>

    @Query("SELECT * FROM app_routing_rules")
    fun getAllRules(): Flow<List<AppRoutingRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AppRoutingRule)

    @Delete
    suspend fun delete(rule: AppRoutingRule)
}

@Dao
interface TrafficStatsDao {
    @Query("SELECT * FROM traffic_stats WHERE appUid = :appUid")
    fun getStatsForApp(appUid: Int): Flow<TrafficStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: TrafficStats)
    
    @Query("UPDATE traffic_stats SET uploadBytes = uploadBytes + :up, downloadBytes = downloadBytes + :down, connectionCount = connectionCount + :conn WHERE appUid = :appUid")
    suspend fun addTrafficStats(appUid: Int, up: Long, down: Long, conn: Long)
}

@Dao
interface ConnectionLogDao {
    @Query("SELECT * FROM connection_logs WHERE appUid = :appUid ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsForApp(appUid: Int, limit: Int): Flow<List<ConnectionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ConnectionLog)
    
    @Query("DELETE FROM connection_logs WHERE timestamp < :olderThanMillis")
    suspend fun deleteOldLogs(olderThanMillis: Long)
}
