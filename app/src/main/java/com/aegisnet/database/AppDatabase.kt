package com.aegisnet.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aegisnet.database.dao.*
import com.aegisnet.database.entity.*

@Database(
    entities = [
        DnsProfile::class,
        FilterList::class,
        UserRule::class,
        WhitelistList::class,
        WhitelistRule::class,
        RoutingRule::class,
        WgProfile::class,
        AppInfo::class,
        AppDomainRule::class,
        AppDNSRule::class,
        AppRoutingRule::class,
        TrafficStats::class,
        ConnectionLog::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun dnsProfileDao(): DnsProfileDao
    abstract fun filterListDao(): FilterListDao
    abstract fun userRuleDao(): UserRuleDao
    abstract fun whitelistListDao(): WhitelistListDao
    abstract fun whitelistRuleDao(): WhitelistRuleDao
    abstract fun routingRuleDao(): RoutingRuleDao
    abstract fun wgProfileDao(): WgProfileDao
    
    abstract fun appInfoDao(): AppInfoDao
    abstract fun appDomainRuleDao(): AppDomainRuleDao
    abstract fun appDNSRuleDao(): AppDNSRuleDao
    abstract fun appRoutingRuleDao(): AppRoutingRuleDao
    abstract fun trafficStatsDao(): TrafficStatsDao
    abstract fun connectionLogDao(): ConnectionLogDao
}
