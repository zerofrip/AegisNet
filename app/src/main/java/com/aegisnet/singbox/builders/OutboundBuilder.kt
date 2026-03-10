package com.aegisnet.singbox.builders

import com.aegisnet.database.entity.WgProfile
import org.json.JSONArray
import org.json.JSONObject

object OutboundBuilder {
    fun build(wgProfile: WgProfile?): JSONArray {
        val outbounds = JSONArray()
        outbounds.put(JSONObject().apply {
            put("type", "direct")
            put("tag", "direct")
        })
        outbounds.put(JSONObject().apply {
            put("type", "block")
            put("tag", "block")
        })
        
        if (wgProfile != null) {
            outbounds.put(WireGuardConfigBuilder.build(wgProfile))
        }
        return outbounds
    }
}
