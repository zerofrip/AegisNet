package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist_lists")
data class WhitelistList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val isEnabled: Boolean = true,
    val updateInterval: Long = 24L * 60L * 60L * 1000L, // Default 24 hours in ms
    val lastUpdated: Long = 0
)
