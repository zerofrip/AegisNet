package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class AppInfo(
    @PrimaryKey val uid: Int,
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)
