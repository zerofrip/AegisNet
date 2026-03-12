package com.aegisnet.singbox.model

import com.aegisnet.database.entity.DnsProfile
import com.aegisnet.database.entity.FilterList
import com.aegisnet.database.entity.RoutingRule
import com.aegisnet.database.entity.WhitelistList
import com.aegisnet.database.entity.WgProfile

/**
 * Encapsulates a complete snapshot of all routing, filtering, and DNS parameters dynamically selected
 * by the user. Handed immutably to the `ConfigGenerator` for final rendering into `config.json`.
 */
data class UserSettings(
    val dnsServers: List<DnsProfile>,
    val fakeDnsEnabled: Boolean,
    val filterLists: List<FilterList>,
    val whitelistLists: List<WhitelistList>,
    val userFilters: List<String>,
    val blockQuic: Boolean,
    val wireGuardProfiles: List<WgProfile>,
    val activeWireGuardProfile: WgProfile?, // Encapsulated Profile object
    val smartRoutingRules: List<RoutingRule>
)
