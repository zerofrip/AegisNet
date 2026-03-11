package com.aegisnet.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_domain_rules",
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
data class AppDomainRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appUid: Int,
    val domain: String,
    val action: String, // "BLOCK", "ALLOW"
    val matchType: String // "EXACT", "SUFFIX", "WILDCARD"
)
