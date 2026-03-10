package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wg_profiles")
data class WgProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val privateKey: String,
    val publicKey: String,
    val endpoint: String,
    val allowedIps: String,
    val dns: String,
    val mtu: Int = 1280,
    val isActive: Boolean = false
)
