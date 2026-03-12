package com.aegisnet.singbox

import android.content.Context
import com.aegisnet.dns.DNSManager
import com.aegisnet.filter.FilterManager
import com.aegisnet.whitelist.WhitelistManager
import com.aegisnet.wireguard.WireGuardManager
import com.aegisnet.singbox.builders.*
import com.aegisnet.singbox.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dnsManager: DNSManager,
    private val filterManager: FilterManager,
    private val whitelistManager: WhitelistManager,
    private val wireGuardManager: WireGuardManager
) {
    
    // Core sing-box JSON Configuration Generator
    fun build(settings: UserSettings): String {
        val root = JSONObject()

        // 1. Log configuration
        val log = JSONObject().apply {
            put("level", "info")
            put("timestamp", true)
        }
        root.put("log", log)

        // 2. DNS configuration
        root.put("dns", DNSConfigBuilder.build(settings))

        // 3. Inbounds
        root.put("inbounds", InboundBuilder.build())

        // 4. Outbounds
        root.put("outbounds", OutboundBuilder.build(settings.activeWireGuardProfile))

        // 5. Routing (Including HTTP filters, Whitelists & Blocks dynamically built through the Filter Engine in reality, but here we construct mapping arrays)
        root.put("route", RoutingConfigBuilder.build(settings))

        val finalJson = root.toString(2)
        
        // 7. Save config.json physically
        saveConfigToDisk(finalJson)
        
        return finalJson
    }

    private fun saveConfigToDisk(jsonContent: String) {
        try {
            val file = File(context.filesDir, "config.json")
            file.writeText(jsonContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
