package com.aegisnet.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aegisnet.database.entity.DnsProfile
import com.aegisnet.database.entity.FilterList
import com.aegisnet.database.entity.UserRule
import com.aegisnet.database.entity.WhitelistList
import com.aegisnet.database.entity.WhitelistRule
import com.aegisnet.database.entity.RoutingRule
import com.aegisnet.database.entity.WgProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DnsProfileDao {
    @Query("SELECT * FROM dns_profiles")
    fun getAll(): Flow<List<DnsProfile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: DnsProfile)
    
    @Delete
    suspend fun delete(profile: DnsProfile)
    
    @Query("UPDATE dns_profiles SET isActive = 0")
    suspend fun deactivateAll()
    
    @Query("UPDATE dns_profiles SET isActive = 1 WHERE id = :id")
    suspend fun activateProfile(id: Long)
}

@Dao
interface FilterListDao {
    @Query("SELECT * FROM filter_lists")
    fun getAll(): Flow<List<FilterList>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: FilterList)
    
    @Delete
    suspend fun delete(list: FilterList)
}

@Dao
interface UserRuleDao {
    @Query("SELECT * FROM user_rules")
    fun getAll(): Flow<List<UserRule>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: UserRule)
    
    @Delete
    suspend fun delete(rule: UserRule)
}

@Dao
interface WhitelistRuleDao {
    @Query("SELECT * FROM whitelist_rules")
    fun getAll(): Flow<List<WhitelistRule>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: WhitelistRule)
    
    @Delete
    suspend fun delete(rule: WhitelistRule)
}

@Dao
interface WhitelistListDao {
    @Query("SELECT * FROM whitelist_lists")
    fun getAll(): Flow<List<WhitelistList>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: WhitelistList)
    
    @Delete
    suspend fun delete(list: WhitelistList)
}

@Dao
interface RoutingRuleDao {
    @Query("SELECT * FROM routing_rules")
    fun getAll(): Flow<List<RoutingRule>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RoutingRule)
    
    @Delete
    suspend fun delete(rule: RoutingRule)
}

@Dao
interface WgProfileDao {
    @Query("SELECT * FROM wg_profiles")
    fun getAll(): Flow<List<WgProfile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: WgProfile)
    
    @Delete
    suspend fun delete(profile: WgProfile)
}
