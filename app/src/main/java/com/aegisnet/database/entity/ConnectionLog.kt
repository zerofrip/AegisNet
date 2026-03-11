package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "connection_logs",
    foreignKeys = [
        ForeignKey(
            entity = AppInfo::class,
            parentColumns = ["uid"],
            childColumns = ["appUid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("appUid")]
)
data class ConnectionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val appUid: Int,
    val domain: String,
    val ip: String,
    val action: String // "BLOCKED", "ALLOWED"
)
