package com.aegisnet.singbox.builders

import com.aegisnet.database.entity.WgProfile
import org.json.JSONArray
import org.json.JSONObject

object WireGuardConfigBuilder {
    fun build(wg: WgProfile): JSONObject {
        return JSONObject().apply {
            put("type", "wireguard")
            put("tag", "wireguard")
            put("local_address", JSONArray(wg.allowedIps.split(",").map { it.trim() }))
            put("private_key", wg.privateKey)
            put("peer_public_key", wg.publicKey)
            put("server", wg.endpoint.substringBeforeLast(":"))
            put("server_port", wg.endpoint.substringAfterLast(":").toIntOrNull() ?: 51820)
            put("mtu", wg.mtu)
        }
    }
}
