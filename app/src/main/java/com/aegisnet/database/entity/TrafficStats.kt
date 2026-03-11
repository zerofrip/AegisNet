package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "traffic_stats",
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
data class TrafficStats(
    @PrimaryKey val appUid: Int,
    val uploadBytes: Long,
    val downloadBytes: Long,
    val connectionCount: Long
)
