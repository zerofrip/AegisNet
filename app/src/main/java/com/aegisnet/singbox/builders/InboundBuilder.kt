package com.aegisnet.singbox.builders

import org.json.JSONArray
import org.json.JSONObject

object InboundBuilder {
    fun build(): JSONArray {
        val inbounds = JSONArray()
        inbounds.put(JSONObject().apply {
            put("type", "tun")
            put("tag", "tun-in")
            put("interface_name", "tun0")
            put("inet4_address", "172.19.0.1/24")
            put("inet6_address", "fdfe:dcba:9876::1/64")
            put("mtu", 1500)
            put("auto_route", false)
            put("strict_route", false)
            put("stack", "gvisor") // gvisor is more stable for Android TUN than system
            put("sniff", true) // SNI sniffing
            put("sniff_override_destination", true)
        })
        return inbounds
    }
}
