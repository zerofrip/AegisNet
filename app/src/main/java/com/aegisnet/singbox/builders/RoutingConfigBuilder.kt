package com.aegisnet.singbox.builders

import com.aegisnet.database.entity.RoutingRule
import com.aegisnet.singbox.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject

object RoutingConfigBuilder {
    fun build(settings: UserSettings): JSONObject {
        val routeRules = JSONArray()

        // 1. Whitelist domains (direct pass-through, highest priority)
        if (settings.whitelistDomains.isNotEmpty()) {
            routeRules.put(JSONObject().apply {
                put("domain", JSONArray(settings.whitelistDomains))
                put("outbound", "direct")
            })
        }

        // 2. Block filter domains
        if (settings.userFilters.isNotEmpty()) {
            routeRules.put(JSONObject().apply {
                put("domain", JSONArray(settings.userFilters))
                put("outbound", "block")
            })
        }

        // 3. Disable QUIC if requested (blocks port 443 UDP)
        if (settings.blockQuic) {
            routeRules.put(JSONObject().apply {
                put("port", 443)
                put("network", "udp")
                put("outbound", "block")
            })
        }

        // 4. SNI / Smart Routing Mappings
        settings.smartRoutingRules.filter { it.isEnabled }.forEach { rule ->
            val jsonRule = JSONObject()
            when (rule.type.lowercase()) {
                "domain" -> jsonRule.put("domain_suffix", JSONArray().put(rule.value))
                "ip" -> jsonRule.put("ip_cidr", JSONArray().put(rule.value))
                "geoip" -> jsonRule.put("geoip", JSONArray().put(rule.value))
                // Support SNI explicit routing (domain_keyword covers most SNI matching natively)
                "sni" -> jsonRule.put("domain_keyword", JSONArray().put(rule.value))
            }
            jsonRule.put("outbound", rule.target.lowercase())
            routeRules.put(jsonRule)
        }
        
        // 5. Fallback - By default, unmatched traffic goes to WireGuard (or direct if WG disabled)
        val defaultOutbound = if (settings.activeWireGuardProfile != null) "wireguard" else "direct"
        routeRules.put(JSONObject().apply {
            put("outbound", defaultOutbound)
        })

        return JSONObject().apply {
            put("rules", routeRules)
            put("auto_detect_interface", true)
        }
    }
}
