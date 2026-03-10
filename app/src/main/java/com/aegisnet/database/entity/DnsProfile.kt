package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dns_profiles")
data class DnsProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // DoH, DoT, UDP, DoQ
    val serverUrl: String,
    val isActive: Boolean = false
)
