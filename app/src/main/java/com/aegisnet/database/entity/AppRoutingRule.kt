package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_routing_rules",
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
data class AppRoutingRule(
    @PrimaryKey val appUid: Int,
    val routeMode: String, // "DIRECT", "WIREGUARD", "BLOCK", "BYPASS"
    val blockQuic: Boolean,
    val bypassVpn: Boolean
)
