package com.aegisnet.singbox.builders

import com.aegisnet.singbox.model.UserSettings
import com.aegisnet.database.entity.WgProfile
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
        
        // Add a fallback DNS server via direct to resolve WG endpoint
        dnsServers.put(JSONObject().apply {
            put("tag", "dns-fallback")
            put("address", "8.8.8.8")
            put("detour", "direct")
        })

        if (settings.dnsServers.isEmpty()) {
            // Fallback DNS (DoH)
            dnsServers.put(JSONObject().apply {
                put("tag", "default-dns")
                put("address", "https://dns.cloudflare.com/dns-query")
                put("detour", targetDetour)
            })
        }

        // Add a block DNS server for filter rules
        dnsServers.put(JSONObject().apply {
            put("tag", "dns-block")
            put("address", "rcode://success")
        })

        return JSONObject().apply {
            put("servers", dnsServers)
            val dnsRules = JSONArray()

            // DNS-level domain blocking from user filter rules
            if (settings.userFilters.isNotEmpty()) {
                dnsRules.put(JSONObject().apply {
                    put("domain", JSONArray(settings.userFilters))
                    put("server", "dns-block")
                })
            }

            // Rule: WireGuard endpoint domain should be resolved via direct DNS
            settings.activeWireGuardProfile?.let { wg ->
                val endpointHost = wg.endpoint.substringBeforeLast(":")
                if (endpointHost.isNotEmpty() && !endpointHost.matches(Regex("^[0-9.]+$"))) {
                    dnsRules.put(JSONObject().apply {
                        put("domain", JSONArray().apply { put(endpointHost as Any) })
                        put("server", "dns-fallback")
                    })
                }
            }
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
