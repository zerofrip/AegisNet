package com.aegisnet.di

import android.content.Context
import androidx.room.Room
import com.aegisnet.database.AegisDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAegisDatabase(@ApplicationContext context: Context): AegisDatabase {
        return Room.databaseBuilder(
            context,
            AegisDatabase::class.java,
            "aegis_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDnsProfileDao(database: AegisDatabase) = database.dnsProfileDao()

    @Provides
    fun provideFilterListDao(database: AegisDatabase) = database.filterListDao()

    @Provides
    fun provideUserRuleDao(database: AegisDatabase) = database.userRuleDao()

    @Provides
    fun provideWhitelistListDao(database: AegisDatabase) = database.whitelistListDao()

    @Provides
    fun provideWhitelistRuleDao(database: AegisDatabase) = database.whitelistRuleDao()

    @Provides
    fun provideRoutingRuleDao(database: AegisDatabase) = database.routingRuleDao()

    @Provides
    fun provideWgProfileDao(database: AegisDatabase) = database.wgProfileDao()
    
    @Provides
    fun provideAppInfoDao(database: AegisDatabase) = database.appInfoDao()

    @Provides
    fun provideAppDomainRuleDao(database: AegisDatabase) = database.appDomainRuleDao()

    @Provides
    fun provideAppDNSRuleDao(database: AegisDatabase) = database.appDNSRuleDao()

    @Provides
    fun provideAppRoutingRuleDao(database: AegisDatabase) = database.appRoutingRuleDao()

    @Provides
    fun provideTrafficStatsDao(database: AegisDatabase) = database.trafficStatsDao()

    @Provides
    fun provideConnectionLogDao(database: AegisDatabase) = database.connectionLogDao()
}

