package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_rules")
data class UserRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rule: String,
    val isEnabled: Boolean = true
)
