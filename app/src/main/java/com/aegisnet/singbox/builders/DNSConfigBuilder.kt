package com.aegisnet.singbox.builders

import com.aegisnet.singbox.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject

object DNSConfigBuilder {
    fun build(settings: UserSettings): JSONObject {
        val dnsServers = JSONArray()
        
        // Add FakeDNS server if enabled
        if (settings.fakeDnsEnabled) {
            dnsServers.put(JSONObject().apply {
                put("tag", "fakedns")
                put("address", "fakeip")
            })
        }

        // Add user configured DNS profiles (ordered by priority/ID essentially)
        val targetDetour = if (settings.activeWireGuardProfile != null) "wireguard" else "direct"
        
        settings.dnsServers.forEachIndexed { index, profile ->
            dnsServers.put(JSONObject().apply {
                put("tag", "dns-$index")
                put("address", profile.serverUrl) // e.g., https://dns.google/dns-query
                put("strategy", "ipv4_only")
                put("detour", targetDetour)
            })
        }
        
        if (settings.dnsServers.isEmpty()) {
            // Fallback DNS (DoH)
            dnsServers.put(JSONObject().apply {
                put("tag", "default-dns")
                put("address", "https://dns.cloudflare.com/dns-query")
                put("detour", targetDetour)
            })
        }

        return JSONObject().apply {
            put("servers", dnsServers)
            val dnsRules = JSONArray()
            // Forward queries to standard DNS except custom rules if any.
            put("rules", dnsRules)
            
            if (settings.fakeDnsEnabled) {
                // Fake IP range
                put("fakeip", JSONObject().apply {
                    put("enabled", true)
                    put("inet4_range", "198.18.0.0/15")
                    put("inet6_range", "fc00::/18")
                })
            }
        }
    }
}
