package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routing_rules")
data class RoutingRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // Domain, IP, GeoIP
    val target: String, // Direct, Block, WireGuard
    val value: String,
    val isEnabled: Boolean = true
)
