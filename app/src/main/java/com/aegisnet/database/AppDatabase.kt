package com.aegisnet.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aegisnet.database.dao.DnsProfileDao
import com.aegisnet.database.dao.FilterListDao
import com.aegisnet.database.dao.UserRuleDao
import com.aegisnet.database.dao.WhitelistListDao
import com.aegisnet.database.dao.WhitelistRuleDao
import com.aegisnet.database.dao.RoutingRuleDao
import com.aegisnet.database.dao.WgProfileDao
import com.aegisnet.database.entity.DnsProfile
import com.aegisnet.database.entity.FilterList
import com.aegisnet.database.entity.UserRule
import com.aegisnet.database.entity.WhitelistList
import com.aegisnet.database.entity.WhitelistRule
import com.aegisnet.database.entity.RoutingRule
import com.aegisnet.database.entity.WgProfile

@Database(
    entities = [
        DnsProfile::class,
        FilterList::class,
        UserRule::class,
        WhitelistList::class,
        WhitelistRule::class,
        RoutingRule::class,
        WgProfile::class
    ],
    version = 3,
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
}
