package com.aegisnet.singbox.builders

import com.aegisnet.database.entity.RoutingRule
import com.aegisnet.singbox.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject

object RoutingConfigBuilder {
    fun build(settings: UserSettings): JSONObject {
        val routeRules = JSONArray()

        // 1. HTTP Filtering (Global block list mapping if enabled)
        // Usually added as a `rule_set`, but since AegisNet uses direct Memory Maps built by SingboxRuleCompiler
        // we omit rule_set here and apply the raw arrays from FilterConfigBuilder inside `ConfigGenerator` flow.

        // 2. Disable QUIC if requested (blocks port 443 UDP)
        if (settings.blockQuic) {
            routeRules.put(JSONObject().apply {
                put("port", 443)
                put("network", "udp")
                put("outbound", "block")
            })
        }

        // 3. SNI / Smart Routing Mappings
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
        
        // 4. Fallback - By default, unmatched traffic goes to WireGuard (or direct if WG disabled)
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
