package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist_rules")
data class WhitelistRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val isEnabled: Boolean = true
)
